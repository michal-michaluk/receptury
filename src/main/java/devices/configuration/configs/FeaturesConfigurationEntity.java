package devices.configuration.configs;

import devices.configuration.intervals.IntervalRules;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "features_configuration")
@AllArgsConstructor
@NoArgsConstructor
public class FeaturesConfigurationEntity {
    @Id
    private String name;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private IntervalRules configuration;

    public FeaturesConfigurationEntity withConfiguration(IntervalRules configuration) {
        this.configuration = configuration;
        return this;
    }
}
