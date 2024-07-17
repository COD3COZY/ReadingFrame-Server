package com.codecozy.server.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CommentDetailResponse(
   Long commentListID,
   String nickname,
   String comment,
   LocalDate reviewDate,
   List<Integer> commentReaction,
   boolean isMyReaction,
   int myReactionCode
) {}
