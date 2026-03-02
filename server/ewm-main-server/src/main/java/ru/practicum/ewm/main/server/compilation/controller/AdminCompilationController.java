package ru.practicum.ewm.main.server.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.main.server.compilation.Compilation;
import ru.practicum.ewm.main.server.compilation.CompilationMapper;
import ru.practicum.ewm.main.server.compilation.CompilationService;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto dto) {
        Compilation entity = compilationMapper.toEntity(dto);
        Compilation saved = compilationService.create(entity, dto.events());
        return compilationMapper.toDto(saved);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable @Positive long compId,
                                 @Valid @RequestBody UpdateCompilationRequest dto) {

        Compilation updated = compilationService.update(
                compId,
                dto.title(),
                dto.pinned(),
                dto.events()
        );

        return compilationMapper.toDto(updated);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long compId) {
        compilationService.delete(compId);
    }
}