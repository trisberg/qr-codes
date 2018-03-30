FROM projectriff/java-function-invoker:0.0.6-snapshot
ARG FUNCTION_JAR=/functions/qr-codes-1.0.jar
ARG FUNCTION_CLASS=qr.Generate
ADD build/libs/qr-codes-1.0.jar $FUNCTION_JAR
ENV FUNCTION_URI file://${FUNCTION_JAR}?handler=${FUNCTION_CLASS}
