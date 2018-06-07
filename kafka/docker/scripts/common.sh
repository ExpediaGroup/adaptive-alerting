# Copyright 2018 Expedia Group, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

show_help() {
cat << EOF
Usage: ${0##*/} [-h] [-p] [-r]

    -h, --help      display this help and exit.
    -p, --package   maven package.
    -r, --run       run docker image.
EOF
}

package=0
run=0
while :; do
    case $1 in
        -h|-\?|--help)
            show_help    # Display a usage synopsis.
            exit
            ;;
        -p|--package)
            package=1
            ;;
        -r|--run)
            run=1
            ;;
        --)              # End of all options.
            shift
            break
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            ;;
        *)               # Default case: No more options, so break out of the loop.
            break
    esac

    shift
done

if [ "$package" == "1" ]; then
    echo 'Packaging...'
    cd ../../..
    mvn clean package -DskipTests
    cd kafka/docker/scripts
fi

cd ..
pwd

echo 'Building...'
rm -rf tmp # Let's keep the temp files for easy validating until next run
mkdir tmp
cp template/* tmp

# simple templating based off {{{var}}} not for advanced usage
echo -e "s/{{{APP_NAME}}}/$APP_NAME/\n\
s/{{{DOTTED_APP_NAME}}}/$DOTTED_APP_NAME/\n\
s/{{{MAIN_CLASS}}}/$MAIN_CLASS/\n" > tmp/vars.sed

sed -i '' -f tmp/vars.sed tmp/Dockerfile
sed -i '' -f tmp/vars.sed tmp/jmxtrans-agent.xml
sed -i '' -f tmp/vars.sed tmp/start-app.sh


# copy .. seems to fail in docker build
cd ..
pwd

docker build -f docker/tmp/Dockerfile -t "$APP_NAME" .


if [ "$run" == "1" ]; then
    echo 'Running...'
    docker run "$APP_NAME"
fi
