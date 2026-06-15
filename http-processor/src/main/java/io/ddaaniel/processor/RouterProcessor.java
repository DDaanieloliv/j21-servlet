package io.ddaaniel.processor;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import io.ddaaniel.annotations.GET;

/**
 * RouterProcessor
 */
@SupportedAnnotationTypes("io.ddaaniel.annotations.GET")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class RouterProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.isEmpty()) return false;

		try {
			JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("io.ddaaniel.generated.GeneratedRouter");
			try (Writer writer = builderFile.openWriter()) {
				writer.write("package io.ddaaniel.generated;\n\n");
				writer.write("import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;\n");
				writer.write("import io.ddaaniel.internal.parser.request.mapper.Request;\n");
				writer.write("import io.ddaaniel.internal.parser.request.response.Response;\n");
				writer.write("import io.ddaaniel.internal.parser.request.header.Headers;\n\n");
				writer.write("public class GeneratedRouter {\n");
				writer.write("    public static void route(Request req, Response res) {\n");
				writer.write("        String target = req.RequestLine.RequestTarget;\n");
				writer.write("        try {\n");
				writer.write("            switch (target) {\n");

				for (Element element : roundEnv.getElementsAnnotatedWith(GET.class)) {
					if (element.getKind() == ElementKind.METHOD) {
						ExecutableElement method = (ExecutableElement) element;
						TypeElement clazz = (TypeElement) method.getEnclosingElement();

						String routePath = method.getAnnotation(GET.class).value();
						String className = clazz.getQualifiedName().toString();
						String methodName = method.getSimpleName().toString();

						writer.write(String.format("                case \"%s\" -> %s.%s(req, res);\n", 
									routePath, className, methodName));
					}
				}

				writer.write("                default -> {\n");
				writer.write("                    res.WriteStatusLine(ResponseStatusCode.STATUS_NOT_FOUND);\n");
				writer.write("                    res.WriteHeaders(res.DefaultHeaders(0).h);\n");
				writer.write("                    res.WriteBody(\"<html><h1>404 Not Found</h1></html>\\n\".getBytes());\n");
				writer.write("                }\n");
				writer.write("            }\n");
				writer.write("        } catch (Exception e) {\n");
				writer.write("            System.err.println(\" -> Router Error: \");\n");
				writer.write("            e.printStackTrace();\n");
				writer.write("        }\n");
				writer.write("    }\n");
				writer.write("}\n");
			}
		} catch (Exception e) {
			// Silencia ou loga erros do compilador
		}
		return true;
	}
}
