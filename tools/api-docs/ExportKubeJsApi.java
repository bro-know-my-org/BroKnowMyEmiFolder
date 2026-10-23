import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/** Parses annotations without resolving or loading any Minecraft/KubeJS classes. */
public final class ExportKubeJsApi {
    public static void main(String[] args) throws Exception {
        Path source = Path.of(args[0]);
        Path output = Path.of(args[1]);
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var files = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            var task = (JavacTask) compiler.getTask(null, files, diagnostics, List.of("-proc:none"), null,
                files.getJavaFileObjects(source));
            var unit = task.parse().iterator().next();
            var errors = diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR).toList();
            require(errors.isEmpty(), "Invalid Java source: " + errors);
            String className = source.getFileName().toString().replace(".java", "");
            var contract = unit.getTypeDecls().stream().filter(t -> t instanceof ClassTree)
                .map(t -> (ClassTree) t).filter(t -> t.getSimpleName().contentEquals(className))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Missing API class " + className));
            require(contract.getKind() == Tree.Kind.INTERFACE, "Expected a public API interface");
            boolean importsContext = unit.getImports().stream().anyMatch(i ->
                i.getQualifiedIdentifier().toString().equals("dev.latvian.mods.rhino.Context"));
            var positions = Trees.instance(task).getSourcePositions();
            var methods = new ArrayList<Map<String, Object>>();
            for (var member : contract.getMembers()) {
                if (!(member instanceof MethodTree method) || method.getReturnType() == null) continue;
                var flags = method.getModifiers().getFlags();
                if (flags.contains(Modifier.PRIVATE) || flags.contains(Modifier.STATIC)) continue;
                var info = info(method.getModifiers());
                var descriptions = new LinkedHashMap<String, String>();
                if (info.containsKey("params")) {
                    var params = info.get("params");
                    var entries = params instanceof NewArrayTree array ? array.getInitializers() : List.of(params);
                    require(entries != null, "@Info.params must be an annotation or annotation array");
                    for (var entry : entries) {
                        require(entry instanceof AnnotationTree, "Expected @Param annotation");
                        var param = (AnnotationTree) entry;
                        require(simpleName(param).equals("Param"), "Expected @Param annotation");
                        var values = values(param);
                        String name = text(values.get("name"));
                        require(descriptions.put(name, text(values.get("value"))) == null,
                            "Duplicate @Param: " + name);
                    }
                }
                var parameters = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < method.getParameters().size(); i++) {
                    var param = method.getParameters().get(i);
                    String type = param.getType().toString();
                    if (type.equals("dev.latvian.mods.rhino.Context") || (importsContext && type.equals("Context"))) {
                        require(i == 0, "Rhino Context must be the leading parameter");
                        continue;
                    }
                    String name = param.getName().toString();
                    require(descriptions.containsKey(name), "Missing @Param on " + method.getName() + ": " + name);
                    parameters.add(Map.of("name", name, "javaType", type, "description", descriptions.remove(name)));
                }
                require(descriptions.isEmpty(), "Stale @Param on " + method.getName() + ": " + descriptions.keySet());
                long line = unit.getLineMap().getLineNumber(positions.getStartPosition(unit, method));
                methods.add(Map.of("name", method.getName().toString(), "description", text(info.get("value")),
                    "parameters", parameters, "returnType", method.getReturnType().toString(), "line", line));
            }
            require(!methods.isEmpty(), "API has no methods");
            var api = Map.of("schemaVersion", 1, "source", args[2], "interface", className,
                "description", text(info(contract.getModifiers()).get("value")), "methods", methods);
            Files.createDirectories(output.toAbsolutePath().getParent());
            Files.writeString(output, json(api) + "\n", StandardCharsets.UTF_8);
        }
    }

    private static Map<String, ExpressionTree> info(ModifiersTree modifiers) {
        var annotation = modifiers.getAnnotations().stream().filter(a -> simpleName(a).equals("Info"))
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Missing @Info: " + modifiers));
        return values(annotation);
    }

    private static String simpleName(AnnotationTree annotation) {
        String name = annotation.getAnnotationType().toString();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static Map<String, ExpressionTree> values(AnnotationTree annotation) {
        var result = new LinkedHashMap<String, ExpressionTree>();
        for (var argument : annotation.getArguments()) {
            if (argument instanceof AssignmentTree assignment) {
                result.put(assignment.getVariable().toString(), assignment.getExpression());
            } else {
                result.put("value", argument);
            }
        }
        return result;
    }

    private static String text(ExpressionTree expression) {
        if (expression instanceof LiteralTree literal && literal.getValue() instanceof String value && !value.isBlank()) {
            return value.strip();
        }
        throw new IllegalArgumentException("Expected nonempty string literal in API annotation: " + expression);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private static String json(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map.keySet().stream().map(Object::toString).sorted()
                .map(key -> json(key) + ":" + json(map.get(key))).collect(Collectors.joining(",", "{", "}"));
        }
        if (value instanceof Collection<?> items) {
            return items.stream().map(ExportKubeJsApi::json).collect(Collectors.joining(",", "[", "]"));
        }
        if (value instanceof Number) return value.toString();
        var result = new StringBuilder("\"");
        for (char c : value.toString().toCharArray()) {
            if (c == '"' || c == '\\') result.append('\\').append(c);
            else if (c < 32) result.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
            else result.append(c);
        }
        return result.append('"').toString();
    }
}
