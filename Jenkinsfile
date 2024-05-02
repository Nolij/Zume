#!/usr/bin/env groovy

pipeline {
    agent any
    
    tools {
        jdk "jdk-21"
    }
    
    stages {
        stage("Setup") {
            steps {
                sh "chmod +x gradlew"
            }
        }

        stage(":build") {
            steps {
                sh "./gradlew build -Pexternal_publish=true"
            }
        }

        stage(":publish") {
            steps {
                sh "./gradlew publish -Pexternal_publish=true"
            }
        }
    }
}