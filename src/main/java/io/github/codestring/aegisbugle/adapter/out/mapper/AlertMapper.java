package io.github.codestring.aegisbugle.adapter.out.mapper;

import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AlertMapper {

    AlertEvent toAlertEvent(BugleEvent alert);
}
