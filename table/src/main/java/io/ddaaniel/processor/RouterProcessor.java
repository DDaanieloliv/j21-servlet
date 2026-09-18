package io.ddaaniel.processor;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;

import io.ddaaniel.annotations.HTTP;

/**
 * RouterProcessor
 */
@SupportedAnnotationTypes("io.ddaaniel.annotations.HTTP")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class RouterProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Messager messager = processingEnv.getMessager();
		if (annotations.isEmpty()) return false;

		try {
			JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("io.ddaaniel.generated.RouteTable");
			try (Writer writer = builderFile.openWriter()) {
				writer.write("package io.ddaaniel.generated;\n\n");
				writer.write("import java.util.Map;\n");
				writer.write("import java.util.HashMap;\n");
				writer.write("import java.util.function.Supplier;\n\n");
				writer.write("public class RouteTable {\n");
				writer.write("    public static Map<String, Supplier<Object>> table() {\n");
				writer.write("        Map<String, Supplier<Object>> routes = new HashMap<>();\n");

				Set<String> instantiatedClasses = new HashSet<>();
				StringBuilder controllerDeclarations = new StringBuilder();
                StringBuilder routeRegistrations = new StringBuilder();

				for (Element element : roundEnv.getElementsAnnotatedWith(HTTP.class)) {
					if (element.getKind() == ElementKind.METHOD) {
						ExecutableElement method = (ExecutableElement) element;
						TypeElement clazz = (TypeElement) method.getEnclosingElement();

						String httpMethod = method.getAnnotation(HTTP.class).method().toUpperCase();
						String routePath = method.getAnnotation(HTTP.class).path();
						String className = clazz.getQualifiedName().toString();
						String methodName = method.getSimpleName().toString();

						if (httpMethod == null || httpMethod.trim().isEmpty()) {
							messager.printMessage(
									Diagnostic.Kind.ERROR,
									" -> Parameter 'method' can not be empty ",
									element
							);	
							return false;
						}

						String routeKey = httpMethod + " " + routePath;
						boolean isStatic = method.getModifiers().contains(javax.lang.model.element.Modifier.STATIC);

						if (isStatic) {
							routeRegistrations.append((String.format(
										"        routes.put(\"%s\", () -> {\n" +
										"            try { return %s.%s(); }\n" +
										"            catch (Exception e) { throw new RuntimeException(e); }\n" +
										"        });\n", routeKey, className, methodName
										)));
						} else {

							String varName = "ctrl_" + className.replace(".", "_");
							if (!instantiatedClasses.contains(className)) {
								controllerDeclarations.append(String.format(
											"        final %s %s = new %s();\n", className, varName, className
											));
								instantiatedClasses.add(className);
							}

							routeRegistrations.append((String.format(
										"        routes.put(\"%s\", () -> {\n" +
										"            try { return %s.%s(); }\n" +
										"            catch (Exception e) { throw new RuntimeException(e); }\n" +
										"        });\n", routeKey, varName, methodName
										)));
						}
					}
				}

				writer.write(controllerDeclarations.toString());
                writer.write(routeRegistrations.toString());
				writer.write("        return routes;\n");
				writer.write("    }\n");
				writer.write("}\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
