from __future__ import print_function
import json
import os
import sys
import yaml
import subprocess

# Tunable Parameters

TOP_LEVEL = os.path.abspath(os.path.dirname(__file__))
DOCKER_COMPOSE_PROJECT_DIR = os.path.join( TOP_LEVEL, 'docker-test-framework','4-2')

# Optional - if defined, script must exist and be executable or a RuntimeError is invoked:
#
# PRE_HOOK_SCRIPT = os.path.normpath( os.path.join(TOP_LEVEL,
#                                     'path/to/build_or_prepare.sh'))

# ------------- INIT for docker-compose

def init():
    
    pre_hook_script = globals().get('PRE_HOOK_SCRIPT','')

    if pre_hook_script:
        if os.access(pre_hook_script, os.X_OK):
            x = subprocess.check_output([pre_hook_script])
            print ("PRE_HOOK_SCRIPT '{}' = \n******** Output: ********\n{}".format(pre_hook_script,x))
        else:
            raise RuntimeError("pre_hook_script '{}' must be executable".format(pre_hook_script))

    # optional processing here
    # example : modifying the docker-compose.yml file

    proj_file = os.path.join(DOCKER_COMPOSE_PROJECT_DIR, 'docker-compose.yml')
    proj = yaml.load(open(proj_file,'r'), Loader = yaml.Loader)
    proj['services']['maven']['command'] = '''sh -c "cd /usr/src/jargon ; mvn -s settings.xml install >/output_logs/test_output.txt 2>&1"'''
    proj['services']['maven']['volumes'] += ['./:/output_logs:rw']
    proj = yaml.dump(proj, open(proj_file,'w'))
    
    return DOCKER_COMPOSE_PROJECT_DIR

    
# ------------- RUN via docker-compose

def run (CI):

    final_config = CI.store_config(
        {
            "yaml_substitutions": {       # -> written to ".env" for docker-compose.yml preprocessor

                ######### these examples are from  https://github.com/irods/python-irodsclient counterpart to this file
                # "python_version" : "3",
                # "client_os_generic": "ubuntu",
                # "client_os_image": "ubuntu:18.04"
            },
            "container_environments": {
                "maven" : {       # -> written to "maven.env", which can then be an env_file in the docker-compose.yml
                    ######
                    # "TESTS_TO_RUN" : ""   # by convention, empty defaults to running all tests
                }
            },
            "container_output_paths": {
                "maven" : {       
                    ".": "./maven-client-output"
                }
            }
        }
    )

    print ('----------\nconfig after CI modify pass\n----------',file=sys.stderr)
    print(json.dumps(final_config,indent=4),file=sys.stderr)

    return CI.run_and_wait_on_client_exit (name_pattern = '^' 'maven' '$')
