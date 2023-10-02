package com.nmontiel.superheroes.antiHero.service;

import com.nmontiel.superheroes.antiHero.entity.AntiHeroEntity;
import com.nmontiel.superheroes.antiHero.repository.AntiHeroRepository;
import com.nmontiel.superheroes.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class AntiHeroService {
    private final AntiHeroRepository repo;
    public Iterable<AntiHeroEntity> findAllAntiHeroes(){
        return repo.findAll();
    }

    public AntiHeroEntity findAntiHeroById(UUID id){
        return findOrThrow(id);
    }

    public void removeAntiHeroById(UUID id){
        findOrThrow(id);
    }

    public AntiHeroEntity addAntiHero(AntiHeroEntity antiHero){
        return repo.save(antiHero);
    }

    public void updateAntiHero(UUID id, AntiHeroEntity antiHero){
        findOrThrow(id);
    }

    private AntiHeroEntity findOrThrow(final UUID id){
        return repo.findById(id).orElseThrow(()-> new NotFoundException("Anti-hero by id " + id + " was not found"));
    }
}
