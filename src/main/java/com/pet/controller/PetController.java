package com.pet.controller;

import com.pet.entity.PetProfile;
import com.pet.entity.PetVaccineRecord;
import com.pet.entity.PetHealthRecord;
import com.pet.service.PetService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/pet")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired
    private PetService petService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    // ========== 宠物档案 ==========

    /**
     * 获取用户宠物列表
     */
    @GetMapping("/list")
    public Result<List<PetProfile>> getUserPets(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<PetProfile> list = petService.getUserPets(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取默认宠物
     */
    @GetMapping("/default")
    public Result<PetProfile> getDefaultPet(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            PetProfile pet = petService.getDefaultPet(userId);
            return Result.success(pet);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{id}")
    public Result<PetProfile> getPetDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            PetProfile pet = petService.getPetDetail(id, userId);
            if (pet != null) {
                return Result.success(pet);
            } else {
                return Result.error("宠物不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加宠物
     */
    @PostMapping("/add")
    public Result<?> addPet(@RequestBody PetProfile pet, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = petService.addPet(pet, userId);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新宠物
     */
    @PutMapping("/update")
    public Result<?> updatePet(@RequestBody PetProfile pet, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = petService.updatePet(pet, userId);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deletePet(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = petService.deletePet(id, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置默认宠物
     */
    @PutMapping("/default/{id}")
    public Result<?> setDefaultPet(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = petService.setDefaultPet(id, userId);
            if (success) {
                return Result.success("设置成功");
            } else {
                return Result.error("设置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 疫苗记录 ==========

    @GetMapping("/vaccine/list/{petId}")
    public Result<List<PetVaccineRecord>> getVaccineRecords(@PathVariable Integer petId) {
        try {
            List<PetVaccineRecord> list = petService.getVaccineRecords(petId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/vaccine/add")
    public Result<?> addVaccineRecord(@RequestBody PetVaccineRecord record) {
        try {
            boolean success = petService.addVaccineRecord(record);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/vaccine/delete/{id}")
    public Result<?> deleteVaccineRecord(@PathVariable Integer id) {
        try {
            boolean success = petService.deleteVaccineRecord(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/vaccine/upcoming")
    public Result<List<PetVaccineRecord>> getUpcomingVaccines(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<PetVaccineRecord> list = petService.getUpcomingVaccines(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 体检记录 ==========

    @GetMapping("/health/list/{petId}")
    public Result<List<PetHealthRecord>> getHealthRecords(@PathVariable Integer petId) {
        try {
            List<PetHealthRecord> list = petService.getHealthRecords(petId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/health/add")
    public Result<?> addHealthRecord(@RequestBody PetHealthRecord record) {
        try {
            boolean success = petService.addHealthRecord(record);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/health/delete/{id}")
    public Result<?> deleteHealthRecord(@PathVariable Integer id) {
        try {
            boolean success = petService.deleteHealthRecord(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}