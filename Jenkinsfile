#!groovy
def config = [:]

node('pipelines') {

  config['user'] = 'ipcrm'
  config['api_url']  = "https://api.distelli.com/${config['user']}"
  config['app_name'] = "pipeilnes_jenkins"
  config['api_token'] = 'ibmmox8a2mspxs5lzh3ozhwe9xyq0smslsmmg' //BAD!

  stage('setup') {
    checkout(scm).each { k,v -> env.setProperty(k, v) }
    pipelines = load("lib/pipelines.groovy")
    pipelines.set_env(config)
  }

  stage('build stuff'){
    try {
      config['push_id'] = pipelines.create_push_event(config)
      config['build_id'] = pipelines.create_build_event(config)

      // COMPILE/PACKAGE/WHATEVER

      sh('distelli push -save-release release_version.out')
      pipelines.update_build_status(config['build_id'],'Success',config)
    } catch (Exception e) {
      pipelines.update_build_status(config['build_id'],'Failed',config)
      error("Failed to build! - ${e}")
    }
  }
}
