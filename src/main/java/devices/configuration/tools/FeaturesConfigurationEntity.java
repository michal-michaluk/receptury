package devices.configuration.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import devices.configuration.JsonConfiguration;
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
@NoArgsConstructor
public class FeaturesConfigurationEntity {
    @Id
    private String name;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private JsonNode configuration;

    public FeaturesConfigurationEntity(String name) {
        this.name = name;
        this.configuration = null;
    }

    public FeaturesConfigurationEntity withConfiguration(Object configuration) {
        this.configuration = JsonConfiguration.OBJECT_MAPPER.valueToTree(configuration);
        return this;
    }

    public <T> T configAs(Class<T> type) {
        try {
            return JsonConfiguration.OBJECT_MAPPER.treeToValue(configuration, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
