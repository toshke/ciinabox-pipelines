/***********************************
IAM Assume Role Step DSL

Assumes an IAM role

example usage
withIAMRole(1234567890,region,roleName) {
  sh 'aws s3 ls'
}
************************************/
@Grab(group='com.amazonaws', module='aws-java-sdk-iam', version='1.11.226')
@Grab(group='com.amazonaws', module='aws-java-sdk-sts', version='1.11.226')

import com.amazonaws.services.securitytoken.*
import com.amazonaws.services.securitytoken.model.*

def call(awsAccountId, region, roleName, body) {
  def accessKeyId = env['AWS_ACCESS_KEY_ID']
  def secretAccessKey = env['AWS_SECRET_ACCESS_KEY']
  def sessionToken = env['AWS_SESSION_TOKEN']
  try {
    def stsCredentials = assumeRole(awsAccountId, region, roleName)
    env['AWS_ACCESS_KEY_ID'] = stsCredentials.getAccessKeyId()
    env['AWS_SECRET_ACCESS_KEY'] = stsCredentials.getSecretAccessKey()
    env['AWS_SESSION_TOKEN'] = stsCredentials.getSessionToken()
    body()
  } finally {
    env.AWS_ACCESS_KEY_ID = accessKeyId
    env.AWS_SECRET_ACCESS_KEY = secretAccessKey
    env.AWS_SESSION_TOKEN = sessionToken
  }
}

@NonCPS
def assumeRole(awsAccountId, region, roleName) {
  def roleArn = "arn:aws:iam::" + awsAccountId + ":role/" + roleName
  def roleSessionName = "sts-session-" + awsAccountId
  println "assuming IAM role ${roleArn}"
  def sts = new AWSSecurityTokenServiceClient()
  if (!region.equals("us-east-1")) {
      sts.setEndpoint("sts." + region + ".amazonaws.com")
  }
  def assumeRoleResult = sts.assumeRole(new AssumeRoleRequest()
            .withRoleArn(roleArn).withDurationSeconds(3600)
            .withRoleSessionName(roleSessionName))
  return assumeRoleResult.getCredentials()
}
