package com.studymedical.backend.infrastructure.dto.response;

import lombok.Data;

@Data
public class Pagination {
    private int page;
    private boolean hasMoreNih;
    private boolean hasMoreLocal;
}
