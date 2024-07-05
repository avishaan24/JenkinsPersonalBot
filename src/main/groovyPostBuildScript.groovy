import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStream

def sendPostRequest(String urlString, String payload) {
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

def url = "http://localhost:3978/api/buildInfo" // your bot endpoint
def buildJobName = manager.envVars["JOB_NAME"]
def buildNumber = manager.envVars['BUILD_NUMBER']
def buildResult = manager.build.result
def buildEndTime = manager.build.startTimeInMillis
def payload = "{\"buildStatus\": \"${buildResult}\",  \"buildNumber\": \"${buildNumber}\", \"buildEndTime\": \"${buildEndTime}\", \"buildJobName\": \"${buildJobName}\"}"



sendPostRequest(url, payload)