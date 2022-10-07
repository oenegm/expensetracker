package com.myCompany.budgetManagement.service;

import com.myCompany.budgetManagement.exception.DeleteDataIntegrityViolationException;
import com.myCompany.budgetManagement.exception.NotEnteredForeignKeyIdException;
import com.myCompany.budgetManagement.exception.NotFoundException;
import com.myCompany.budgetManagement.model.Household;
import com.myCompany.budgetManagement.model.User;
import com.myCompany.budgetManagement.repository.HouseholdRepository;
import com.myCompany.budgetManagement.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseholdService {

    private HouseholdRepository householdRepository;
    private UserRepository userRepository;

    public HouseholdService(HouseholdRepository householdRepository, UserRepository userRepository) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
    }

    public List<Household> findAll() {
        return householdRepository.findAll();
    }

    public Household findById(Long id) {
        return householdRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Household not found with id = " + id));
    }

    public Household findByInvitationCode(String invitationCode) {
        return householdRepository
                .findByInvitationCode(invitationCode)
                .orElseThrow(() -> new NotFoundException("Household not found with code = " + invitationCode));
    }

    public Household create(Household household) {
        try {
           return householdRepository.save(household);
        } catch (InvalidDataAccessApiUsageException | DataIntegrityViolationException e){
            throw new NotEnteredForeignKeyIdException("body request should have: {name, totalBalance}");
        }
    }

    public Household update(Long id, Household household) {
        if (!householdRepository.existsById(id)) {
            throw new NotFoundException("Household not found with id = " + id);
        }

        household.setId(id);
        try {
            return householdRepository.save(household);
        } catch (InvalidDataAccessApiUsageException | DataIntegrityViolationException e){
            throw new NotEnteredForeignKeyIdException("body request should have: {name, totalBalance}");
        }
    }

    public void deleteById(Long id) {
        try {
            householdRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Household not found with id = " + id);
        } catch (DataIntegrityViolationException e) {
            // TODO: household and FK
            throw new DeleteDataIntegrityViolationException("Cannot delete household: " +
                    "Must Delete all Transactions or/and the all Members leaves the household");
        }
    }

    // ---------------------------------------------------------------------------

    public List<User> findAllMembers(Long householdId) {
        Household household = findById(householdId);
        return household.getMembers();
    }

    // TODO: use UserService instead
    public void addMember(Long householdId, Long memberId) {
        Household household = findById(householdId);
        User member = userRepository
                .findById(memberId)
                .orElseThrow(() -> new NotFoundException("User not found with id = " + memberId));

        member.setHousehold(household);
        household.getMembers().add(member);

        householdRepository.save(household);
        userRepository.save(member);
    }

    // TODO: use UserService instead
    public void deleteMember(Long householdId, Long memberId) {
        Household household = findById(householdId);
        User member = userRepository
                .findById(memberId)
                .orElseThrow(() -> new NotFoundException("User not found with id = " + memberId));

        member.setHousehold(null);
        household.getMembers().remove(member);

        householdRepository.save(household);
        userRepository.save(member);
    }

}
