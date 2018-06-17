#!groovy
def config = [:]

node('pipelines') {

  config['user'] = 'timidri'
  config['api_url']  = "https://api.distelli.com/${config['user']}"
  config['app_name'] = "rsvp-jenkins"
  config['api_token'] = 'qek38duwidb7i6r160ti8kx40p4dn7m5idfr4' //BAD!

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
