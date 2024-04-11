package com.jk.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {

    private  Long categoryId;

    @NotBlank
    @Size(min = 4, message = "Min Size of Category Title is 4")
    private  String categoryTitle;

    @NotBlank
    @Size(min = 10, message = "Min Size of Category Description is 10")
    private  String categoryDescription;

}
