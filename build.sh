native-image --no-fallback --enable-all-security-services --report-unsupported-elements-at-runtime \
--install-exit-handlers --allow-incomplete-classpath --initialize-at-build-time=io.ktor,kotlinx,kotlin,org.slf4j\
-H:+ReportUnsupportedElementsAtRuntime -H:+ReportExceptionStackTraces -H:ConfigurationFileDirectories=./ -cp ./build/libs/lotus-cli-1.0-all.jar -H:Class=MainKt -H:Name=lotus-cli
