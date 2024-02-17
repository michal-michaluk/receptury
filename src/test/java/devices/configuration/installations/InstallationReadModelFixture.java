package devices.configuration.installations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InstallationReadModelFixture {

    @Autowired
    InstallationReadModel.JpaRepository jpa;

    @Transactional
    public void truncate() {
        jpa.truncate();
    }
}
