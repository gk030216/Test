package com.pet.controller.admin;

import com.pet.entity.PetProfile;
import com.pet.entity.PetVaccineRecord;
import com.pet.entity.PetHealthRecord;
import com.pet.entity.User;
import com.pet.mapper.PetProfileMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.PetService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pet")
@CrossOrigin(origins = "*")
public class AdminPetController {

    @Autowired
    private PetService petService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetProfileMapper petProfileMapper;


    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    private Integer getUserRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    /**
     * 获取所有宠物列表（管理员和员工）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getPetList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = petService.getAdminPetList(page, pageSize, keyword, type, status);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{id}")
    public Result<PetProfile> getPetDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            PetProfile pet = petService.getPetDetail(id, null);
            if (pet != null) {
                return Result.success(pet);
            } else {
                return Result.error("宠物不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加宠物（管理员和员工）
     */
    @PostMapping("/add")
    public Result<?> addPet(@RequestBody PetProfile pet, HttpServletRequest request) {
        try {
            Integer currentUserId = getUserId(request);
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }

            if (pet.getUserId() == null || pet.getUserId() <= 0) {
                return Result.error("请选择所属用户");
            }

            User user = userMapper.findById(pet.getUserId());
            if (user == null) {
                return Result.error("选择的用户不存在");
            }

            // 如果要设为默认，先清除该用户的其他默认宠物
            if (pet.getIsDefault() != null && pet.getIsDefault() == 1) {
                petProfileMapper.clearDefault(pet.getUserId());
            }

            boolean success = petService.addPet(pet, currentUserId);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    /**
     * 更新宠物（管理员和员工）
     */
    @PutMapping("/update")
    public Result<?> updatePet(@RequestBody PetProfile pet, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.updatePet(pet, userId);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除宠物（管理员和员工）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deletePet(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.adminDeletePet(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除宠物
     */
    @DeleteMapping("/batch-delete")
    public Result<?> batchDeletePets(@RequestParam String ids, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (petService.adminDeletePet(Integer.parseInt(idStr))) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> stats = petService.getPetStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // ========== 疫苗记录管理 ==========

    /**
     * 获取疫苗记录列表
     */
    @GetMapping("/vaccine/list")
    public Result<List<PetVaccineRecord>> getVaccineRecords(@RequestParam Integer petId, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            List<PetVaccineRecord> list = petService.getVaccineRecords(petId);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有疫苗记录（管理员和员工）
     */
    @GetMapping("/vaccine/all")
    public Result<Map<String, Object>> getAllVaccineRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = petService.getAllVaccineRecords(page, pageSize, keyword);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有体检记录（管理员和员工）
     */
    @GetMapping("/health/all")
    public Result<Map<String, Object>> getAllHealthRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = petService.getAllHealthRecords(page, pageSize, keyword);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加疫苗记录
     */
    @PostMapping("/vaccine/add")
    public Result<?> addVaccineRecord(@RequestBody PetVaccineRecord record, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.addVaccineRecord(record);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除疫苗记录
     */
    @DeleteMapping("/vaccine/delete/{id}")
    public Result<?> deleteVaccineRecord(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.deleteVaccineRecord(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // ========== 体检记录管理 ==========

    /**
     * 获取体检记录列表
     */
    @GetMapping("/health/list")
    public Result<List<PetHealthRecord>> getHealthRecords(@RequestParam Integer petId, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            List<PetHealthRecord> list = petService.getHealthRecords(petId);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加体检记录
     */
    @PostMapping("/health/add")
    public Result<?> addHealthRecord(@RequestBody PetHealthRecord record, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.addHealthRecord(record);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除体检记录
     */
    @DeleteMapping("/health/delete/{id}")
    public Result<?> deleteHealthRecord(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            // 修改：允许员工和管理员访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.deleteHealthRecord(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新疫苗记录
     */
    @PutMapping("/vaccine/update")
    public Result<?> updateVaccineRecord(@RequestBody PetVaccineRecord record) {
        try {
            boolean success = petService.updateVaccineRecord(record);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新体检记录
     */
    @PutMapping("/health/update")
    public Result<?> updateHealthRecord(@RequestBody PetHealthRecord record) {
        try {
            boolean success = petService.updateHealthRecord(record);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除疫苗记录
     */
    @DeleteMapping("/vaccine/batch-delete")
    public Result<?> batchDeleteVaccineRecords(@RequestBody List<Integer> ids, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.batchDeleteVaccineRecords(ids);
            if (success) {
                return Result.success("批量删除成功，共删除 " + ids.size() + " 条记录");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除体检记录
     */
    @DeleteMapping("/health/batch-delete")
    public Result<?> batchDeleteHealthRecords(@RequestBody List<Integer> ids, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = petService.batchDeleteHealthRecords(ids);
            if (success) {
                return Result.success("批量删除成功，共删除 " + ids.size() + " 条记录");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取即将到期的宠物数量（7天内需要接种疫苗的宠物）
     */
    @GetMapping("/vaccine/upcoming/pets/count")
    public Result<Integer> getUpcomingPetsCount(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            int count = petService.getUpcomingPetsCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取有疫苗记录的正常宠物数量（去重）
     */
    @GetMapping("/vaccine/pets/count")
    public Result<Integer> getDistinctPetsWithVaccineCount(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            int count = petService.getDistinctPetsWithVaccineCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取有体检记录的正常宠物数量（去重）
     */
    @GetMapping("/health/pets/count")
    public Result<Integer> getDistinctPetsWithHealthRecordCount(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            int count = petService.getDistinctPetsWithHealthRecordCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取正常宠物的平均体重
     */
    @GetMapping("/health/avg-weight")
    public Result<BigDecimal> getAvgWeight(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            BigDecimal avgWeight = petService.getAvgWeight();
            return Result.success(avgWeight);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}