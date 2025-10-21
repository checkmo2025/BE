package checkmo.domain.club.repository;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;

import java.util.List;

public interface ClubRepositoryCustom {
    List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size);
}
