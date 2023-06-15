package devices.configuration.tools;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@Configuration
public class JsonConfiguration implements ObjectMapperSupplier {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.CREATOR, ANY)
            .setVisibility(PropertyAccessor.FIELD, ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);


    @Bean
    ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    @Bean
    MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        return new MappingJackson2HttpMessageConverter(OBJECT_MAPPER);
    }

    @Override
    public ObjectMapper get() {
        return OBJECT_MAPPER;
    }

    @PostConstruct
    private void initEventTypes() {
        DeserializationConfig config = OBJECT_MAPPER.getDeserializationConfig();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        Map<Class<?>, EventTypes.Type> subtypes = Stream.of(
                        devices.configuration.device.DomainEvent.class,
                        devices.configuration.installations.DomainEvent.class
                )
                .map(type -> AnnotatedClassResolver.resolve(config, OBJECT_MAPPER.constructType(type), config))
                .map(ai::findSubtypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        NamedType::getType,
                        type -> EventTypes.Type.of(type.getName())
                ));
        EventTypes.init(subtypes);
    }
}
