import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def set_env(config){
    env.GIT_AUTHOR_NAME = sh(returnStdout: true, script: "git --no-pager show -s --format='%an'").trim()
    DISTELLI_USERNAME = config['user']
    DISTELLI_API_URL = config['api_url']
    DISTELLI_APP_NAME = config['app_name']
    DISTELLI_API_TOKEN = config['api_token']
    DISTELLI_CHANGE_TITLE = sh(returnStdout: true, script: 'git --no-pager log --pretty=format:"%s" -1')
    DISTELLI_BUILD_URL= env.BUILD_URL
    DISTELLI_CHANGE_AUTHOR = env.GIT_AUTHOR_NAME
    DISTELLI_CHANGE_AUTHOR_DISPLAY_NAME = env.GIT_AUTHOR_NAME
    DISTELLI_CHANGE_ID = env.GIT_COMMIT
    DISTELLI_BRANCH_NAME = env.GIT_BRANCH.split('/')[1]
    DISTELLI_CHANGE_TARGET = env.GIT_URL
    DISTELLI_CHANGE_URL = "${env.GIT_URL}/commits/${DISTELLI_CHANGE_ID}"
    PIPELINES_API_TOKEN = config['api_token']
}

def create_push_event(config){
  stage('Create Push Event'){
    set_env(config)
    def pipeargs = "apps/${DISTELLI_APP_NAME}/events/pushEvent?apiToken=${PIPELINES_API_TOKEN}"
    def data = [:]
    data['author_username'] = DISTELLI_CHANGE_AUTHOR
    data['author_name'] = DISTELLI_CHANGE_AUTHOR_DISPLAY_NAME
    data['commit_msg'] = DISTELLI_CHANGE_TITLE 
    data['commit_url'] = DISTELLI_CHANGE_URL
    data['repo_url']  = DISTELLI_CHANGE_TARGET
    data['commit_id'] = DISTELLI_CHANGE_ID 
    data['repo_owner'] = 'ipcrm' 
    data['repo_name'] = 'pipelines_jenkins'
    data['branch'] = DISTELLI_BRANCH_NAME

    return pushData('PUT',config['api_url'],pipeargs,data)['event_id']
  }
}

def create_build_event(config){
  set_env(config)
  def DISTELLI_NOW = sh(returnStdout: true, script: 'date -u +%Y-%m-%dT%H:%M:%S.0Z').trim()
  def builddata = [:]
  builddata["build_status"] = 'running'
  builddata["build_start"] = DISTELLI_NOW
  builddata["build_provider"] = 'jenkins'
  builddata["build_url"] = DISTELLI_BUILD_URL
  builddata["repo_url"] = DISTELLI_CHANGE_TARGET 
  builddata["commit_url"] = DISTELLI_CHANGE_URL
  builddata["author_username"] = DISTELLI_CHANGE_AUTHOR
  builddata["author_name"] = DISTELLI_CHANGE_AUTHOR_DISPLAY_NAME
  builddata["commit_msg"] = DISTELLI_CHANGE_TITLE
  builddata["commit_id"] = DISTELLI_CHANGE_ID
  builddata["branch"] = DISTELLI_BRANCH_NAME
  builddata["parent_event_id"] = config['push_id']
  
  def pipeargs = "apps/${config['app_name']}/events/buildEvent?apiToken=${PIPELINES_API_TOKEN}"
  return pushData('PUT',config['api_url'],pipeargs,builddata)['event_id']
}

def update_build_status(build_event_id,status,config){
  set_env(config)
  DISTELLI_NOW = sh(returnStdout: true, script: 'date -u +%Y-%m-%dT%H:%M:%S.0Z').trim()

  def eventdata = [:]
  eventdata['build_status'] = status
  eventdata['build_end'] = DISTELLI_NOW

  if (fileExists('release_version.out')) {
    eventdata['release_version'] = sh(returnStdout: true, script:'cat release_version.out').trim()
  }

  def eventargs = "apps/${config['app_name']}/events/${build_event_id}?apiToken=${PIPELINES_API_TOKEN}"
  pushData('POST',config['api_url'],eventargs,eventdata)
}

def pushData (method,baseurl,args,payload) {
  def jsonSlurper = new JsonSlurper()
  try {
    def fullurl = "${baseurl}/${args}"
    def post = new URL(fullurl).openConnection();
    post.setRequestMethod(method)
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "application/json")
    post.getOutputStream().write(JsonOutput.toJson(payload).getBytes("UTF-8"));
    def postRC = post.getResponseCode();
    if(postRC.equals(200)) {
      def object = jsonSlurper.parseText(post.getInputStream().getText());
      return object
    }else{
      error("POST to ${baseurl} failed! Response code ${postRC.toString()}")
    }
  } catch (Exception e) {
    throw e
  }
}

return this;
