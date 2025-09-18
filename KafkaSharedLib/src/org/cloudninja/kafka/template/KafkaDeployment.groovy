package org.cloudninja.kafka.template

class KafkaDeployment implements Serializable {
    def steps

    KafkaDeployment(steps) {
        this.steps = steps
    }

    def checkoutCode(String repo, String branch) {
        steps.echo "Checking out Git repository..."
        steps.checkout([$class: 'GitSCM',
                        branches: [[name: "*/${branch}"]],
                        userRemoteConfigs: [[url: repo]]])
    }

    def credentialScan(String repo) {
        steps.echo "Running credential scan with GitLeaks..."
        steps.sh "git secrets || true"
        steps.sh "gitleaks detect --source . --report-path gitleaks-report.json || true"
    }

    def ansibleLint(String playbook) {
        steps.echo "Running ansible-lint on playbook..."
        steps.sh "export ANSIBLE_HOST_KEY_CHECKING=False && ansible-lint ${playbook}"
    }

    def ansibleSyntaxCheck(String inventory, String playbook) {
        steps.echo "Checking Ansible playbook syntax..."
        steps.sh "export ANSIBLE_HOST_KEY_CHECKING=False && ansible-playbook -i ${inventory} ${playbook} --syntax-check"
    }

    def ansibleDryRun(String inventory, String playbook, String user, String key) {
        steps.echo "Performing dry run on playbook..."
        steps.sh "export ANSIBLE_HOST_KEY_CHECKING=False && ansible-playbook -i ${inventory} ${playbook} --check -u ${user} --private-key ${key}"
    }

    def deployZookeeper(String inventory, String playbook, String user, String key) {
        steps.echo "Deploying Zookeeper cluster..."
        steps.sh "export ANSIBLE_HOST_KEY_CHECKING=False && ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags zookeeper"
    }

    def deployKafka(String inventory, String playbook, String user, String key) {
        steps.echo "Deploying Kafka cluster..."
        steps.sh "export ANSIBLE_HOST_KEY_CHECKING=False && ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags kafka"
    }
}
