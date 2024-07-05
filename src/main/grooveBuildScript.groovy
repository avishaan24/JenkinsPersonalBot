import groovy.json.JsonOutput
import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStream
import jenkins.model.Jenkins

// Method to send a POST request using HttpURLConnection
def sendPostRequestToURL(String urlString, String payload) {
    URL url = new URL(urlString)
    HttpURLConnection connection = (HttpURLConnection) url.openConnection()
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setDoOutput(true)

    OutputStream outputStream = connection.getOutputStream()
    outputStream.write(payload.getBytes("UTF-8"))
    outputStream.flush()
    outputStream.close()

    int responseCode = connection.getResponseCode()

    println "Response code: ${responseCode}"

    connection.disconnect()
}

// Method to collect recent build parameters and send them to Spring Boot endpoint
def collectAndSendBuildInfo() {
    // Replace with your job name in Jenkins
    def jobName = "Your Job Name"
    def currentJob = Jenkins.instance.getItemByFullName(jobName)
    def jobBuilds = currentJob.getBuilds()

    // Get the most recent build
    def recentBuild = jobBuilds.first()

    // Get recent build parameters
    def recentParameters = getBuildParameters(recentBuild)

    // Get user email associated with the build cause
    def userEmail = getUserEmail(recentBuild)

    // Convert recentParameters to JSON
    def url = 'http://localhost:3978/api/buildInfo' // your bot endpoint
    def payload = JsonOutput.toJson([
            buildJobName: jobName,
            buildNumber: recentBuild.number,
            buildUser: recentBuild?.actions?.findAll { it instanceof hudson.model.CauseAction }?.collect { it.causes }?.flatten()?.find { it instanceof hudson.model.Cause.UserIdCause }?.userName,
            parameters: recentParameters,
            buildUserEmail: userEmail,
            buildStartTime: recentBuild.timeInMillis,
            buildUrl:recentBuild.getAbsoluteUrl()
    ])

    // Send POST request to the Spring Boot endpoint with build information
    sendPostRequestToURL(url, payload)
}


// Method to get the parameters of a build
def getBuildParameters(build) {
    def parametersAction = build.getAction(hudson.model.ParametersAction.class)
    def parameters = [:]
    if (parametersAction != null) {
        parametersAction.each { parameter ->
            parameters[parameter.name] = parameter.value.toString()
        }
    }
    return parameters
}

def getUserEmail(build) {
    def userEmail = ""
    def causeAction = build.getAction(hudson.model.CauseAction.class)
    if (causeAction != null) {
        def causes = causeAction.getCauses()
        causes.each { cause ->
            if (cause instanceof hudson.model.Cause.UserIdCause) {
                def userId = cause.getUserId()
                println "User ID: ${userId}" // Debug print
                def user = Jenkins.instance.getUser(userId)
                if (user != null) {
                    userEmail = user.getProperty(hudson.tasks.Mailer.UserProperty)?.address
                    println "User Email: ${userEmail}" // Debug print
                    return userEmail
                } else {
                    println "User not found for ID: ${userId}" // Debug print
                }
            }
        }
    }
    return userEmail
}


// Main script execution
try{
    collectAndSendBuildInfo()
} catch(Exception e){
    println "Error Occured"
}