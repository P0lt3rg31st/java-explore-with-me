package ru.practicum.ewm.main.server.compilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        List<Compilation> compilations = compilationService.getPublic(pinned, from, size);
        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable @Positive long compId) {
        Compilation compilation = compilationService.getById(compId);
        return compilationMapper.toDto(compilation);
    }
}