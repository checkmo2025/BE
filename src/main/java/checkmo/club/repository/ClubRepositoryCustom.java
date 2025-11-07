package checkmo.club.repository;

import checkmo.club.entity.Club;
import checkmo.club.web.dto.club.ClubRequestDTO;

import java.util.List;

public interface ClubRepositoryCustom {
    List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size);
}
