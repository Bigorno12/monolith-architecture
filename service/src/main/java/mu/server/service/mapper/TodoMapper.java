package mu.server.service.mapper;

import mu.server.persistence.entity.Todo;
import mu.server.service.dto.TodoDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    Todo mapToEntity(TodoDto todoDto);

    @InheritInverseConfiguration
    TodoDto mapToDto(Todo todo);
}
