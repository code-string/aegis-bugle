package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.domain.model.AlertEvent;
import io.github.codestring.aegisbugle.application.domain.model.BugleEvent;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AlertMapper {

    AlertEvent toAlertEvent(BugleEvent alert);
}
