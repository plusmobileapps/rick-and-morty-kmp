Pod::Spec.new do |spec|
    spec.name                     = 'rick_and_morty_api'
    spec.version                  = '0.3-alpha01'
    spec.homepage                 = 'https://github.com/plusmobileapps/rick-and-morty-kmp'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Rick and Morty KMP API Http Client'
    spec.vendored_frameworks      = 'build/cocoapods/framework/rick-and-morty-api.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '14.1'
                
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':rick-and-morty-api',
        'PRODUCT_MODULE_NAME' => 'rick-and-morty-api',
    }
                
    spec.script_phases = [
        {
            :name => 'Build rick_and_morty_api',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end