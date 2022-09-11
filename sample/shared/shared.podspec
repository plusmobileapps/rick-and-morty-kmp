Pod::Spec.new do |spec|
    spec.name                     = 'shared'
    spec.version                  = '0.1-alpha08'
    spec.homepage                 = 'https://github.com/plusmobileapps/rick-and-morty-kmp'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Rick and Morty KMP SDK'
    spec.vendored_frameworks      = 'build/cocoapods/framework/rickandmortysdk.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '14.1'
                
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':sample:shared',
        'PRODUCT_MODULE_NAME' => 'rickandmortysdk',
    }
                
    spec.script_phases = [
        {
            :name => 'Build shared',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$COCOAPODS_SKIP_KOTLIN_BUILD" ]; then
                  echo "Skipping Gradle build task invocation due to COCOAPODS_SKIP_KOTLIN_BUILD environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end