package org.cloudninja.kafka.template

class KafkaDeployment implements Serializable {
    def steps

    KafkaDeployment(steps) {
        this.steps = steps
    }

    // ===== Git Checkout =====
    def checkoutCode(String repo, String branch) {
        steps.echo "Checking out Git repository..."
        steps.checkout([$class: 'GitSCM',
                        branches: [[name: "*/${branch}"]],
                        userRemoteConfigs: [[url: repo]]
        ])
    }

    // ===== Ansible Lint =====
    def ansibleLint(String playbook) {
        steps.echo "Running ansible-lint on playbook..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-lint ${playbook}
        """
    }

    // ===== Ansible Syntax Check =====
    def ansibleSyntaxCheck(String inventory, String playbook) {
        steps.echo "Checking Ansible playbook syntax..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} --syntax-check
        """
    }

    // ===== Ansible Dry Run =====
    def ansibleDryRun(String inventory, String playbook, String user, String key) {
        steps.echo "Performing dry run (check mode) on playbook..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} --check -u ${user} --private-key ${key}
        """
    }

    // ===== Deploy Zookeeper =====
    def deployZookeeper(String inventory, String playbook, String user, String key) {
        steps.echo "Deploying Zookeeper cluster..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags zookeeper
        """
    }

    // ===== Deploy Kafka =====
    def deployKafka(String inventory, String playbook, String user, String key) {
        steps.echo "Deploying Kafka cluster..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags kafka
        """
    }
}
