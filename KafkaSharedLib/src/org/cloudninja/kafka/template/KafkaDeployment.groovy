package org.cloudninja.kafka.template

class KafkaDeployment implements Serializable {
    def steps

    KafkaDeployment(steps) {
        this.steps = steps
    }

    def checkoutCode(String repo, String branch) {
        steps.stage('Checkout Code') {
            steps.echo "Checking out Git repository..."
            steps.checkout([$class: 'GitSCM',
                            branches: [[name: "*/${branch}"]],
                            userRemoteConfigs: [[url: repo]]])
        }
    }

    def ansibleLint(String playbook) {
        steps.stage('Ansible Lint') {
            steps.echo "Running ansible-lint..."
            steps.sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-lint ${playbook}
            """
        }
    }

    def ansibleSyntaxCheck(String inventory, String playbook) {
        steps.stage('Ansible Syntax Check') {
            steps.echo "Checking playbook syntax..."
            steps.sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-playbook -i ${inventory} ${playbook} --syntax-check
            """
        }
    }

    def ansibleDryRun(String inventory, String playbook, String user, String key) {
        steps.stage('Ansible Dry Run (Check Mode)') {
            steps.echo "Performing dry run..."
            steps.sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-playbook -i ${inventory} ${playbook} --check -u ${user} --private-key ${key}
            """
        }
    }

    def deployZookeeper(String inventory, String playbook, String user, String key) {
        steps.stage('Deploy Zookeeper') {
            steps.echo "Deploying Zookeeper cluster..."
            steps.sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags zookeeper
            """
        }
    }

    def deployKafka(String inventory, String playbook, String user, String key) {
        steps.stage('Deploy Kafka') {
            steps.echo "Deploying Kafka cluster..."
            steps.sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags kafka
            """
        }
    }
}
