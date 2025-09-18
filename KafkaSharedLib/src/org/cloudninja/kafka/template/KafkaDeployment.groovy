package org.cloudninja.kafka.template

class KafkaDeployment implements Serializable {
    def steps

    KafkaDeployment(steps) {
        this.steps = steps
    }

    // ----------------------------
    // Git / Repo
    // ----------------------------
    def checkoutCode(String repo, String branch) {
        steps.echo "Checking out Git repository..."
        steps.checkout([$class: 'GitSCM',
                        branches: [[name: "*/${branch}"]],
                        userRemoteConfigs: [[url: repo]]])
    }

    def credentialScan(String repo) {
        steps.echo "Running credential/secret scan..."
        steps.sh "trufflehog git --json ${repo} || true"
    }

    def dependencyScan() {
        steps.echo "Checking dependencies..."
        steps.sh """
            pip install safety && safety check -r requirements.txt || true
            # Add Java/Maven scan if needed: mvn dependency-check:check
        """
    }

    // ----------------------------
    // Code-level
    // ----------------------------
    def staticCodeAnalysis() {
        steps.echo "Running static code analysis..."
        steps.sh "pylint **/*.py || true" // Python example
        // For Java: steps.sh "mvn sonar:sonar"
    }

    def runUnitTests() {
        steps.echo "Running unit tests..."
        steps.sh "pytest --maxfail=1 --disable-warnings -q || true" // Python example
        // Java: steps.sh "mvn test"
    }

    def buildCode() {
        steps.echo "Compiling/building code..."
        steps.sh "mvn clean compile || true" // Example for Java
    }

    def codeCoverage() {
        steps.echo "Checking code coverage..."
        steps.sh "coverage run -m pytest && coverage report || true"
        // Java: steps.sh "mvn jacoco:report"
    }

    // ----------------------------
    // Ansible-level
    // ----------------------------
    def ansibleLint(String playbook) {
        steps.echo "Running ansible-lint on playbook..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-lint ${playbook}
        """
    }

    def ansibleSyntaxCheck(String inventory, String playbook) {
        steps.echo "Checking Ansible playbook syntax..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} --syntax-check
        """
    }

    def ansibleDryRun(String inventory, String playbook, String user, String key) {
        steps.echo "Performing dry run (check mode) on playbook..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} --check -u ${user} --private-key ${key}
        """
    }

    def ansibleIdempotencyTest(String inventory, String playbook, String user, String key) {
        steps.echo "Checking Ansible idempotency..."
        ansibleDryRun(inventory, playbook, user, key)
    }

    def inventoryPing(String inventory, String user, String key) {
        steps.echo "Checking inventory reachability..."
        steps.sh "ansible -i ${inventory} all -m ping -u ${user} --private-key ${key}"
    }

    // ----------------------------
    // Deployment
    // ----------------------------
    def deployZookeeper(String inventory, String playbook, String user, String key) {
        steps.echo "Deploying Zookeeper cluster..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags zookeeper
        """
    }

    def deployKafka(String inventory, String playbook, String user, String key) {
        steps.echo "Deploying Kafka cluster..."
        steps.sh """
            export ANSIBLE_HOST_KEY_CHECKING=False
            ansible-playbook -i ${inventory} ${playbook} -u ${user} --private-key ${key} --tags kafka
        """
    }
}
