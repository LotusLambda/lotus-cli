## Building

Install graalvm:
https://github.com/graalvm/homebrew-tap

Install native-image using graalvm
`gu install native-image`

Then when in dir:
`./gradlew shadowJar`
`./build.sh`