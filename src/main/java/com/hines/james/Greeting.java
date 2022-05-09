package com.hines.james;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Greeting {
    private final long id;
    private final String content;
}
