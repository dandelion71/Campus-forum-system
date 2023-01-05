package com.dandelion.admin.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.config.SecurityConfig;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.*;
import com.dandelion.system.mapper.PostsMapper;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.*;
import com.dandelion.system.vo.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/reception/user")
public class ReceptionUserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostsService postsService;

    @Autowired
    private PostsMapper postsMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MutedService mutedService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/searchUser")
    public ResponseResult searchUser(@RequestParam String value) {
        List<UserVo> userList = userMapper.getUserByKeyword(value);
        return ResponseResult.success(userList);
    }

    @GetMapping("/query/byId/{id}")
    public ResponseResult getOneById(@PathVariable String id) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .ne(User::getDelFlag, 2)
                .eq(User::getId, id));
        user.setRole(userMapper.getRole(user.getId()));
        return ResponseResult.success(user);
    }

    @GetMapping("/queryUserInfo/{userId}")
    public ResponseResult queryUserInfo(@PathVariable String userId) {
        isMuted(userId);
        PostUserVo postUserVo = userMapper.getPostUserById(userId);
        setPostsUser(postUserVo);
        postUserVo.setRole(userMapper.getRole(postUserVo.getId()).getRoleName());
        postUserVo.setCollectionPostNum(userMapper.selectUserCollectionPost(userId));
        if("1".equals(postUserVo.getMuted())){
            List<Muted> mutedList = mutedService.list(new LambdaQueryWrapper<Muted>()
                    .eq(Muted::getUserId, SecurityUtils.getUserId())
                    .ne(Muted::getEffective, 1)
                    .orderByDesc(Muted::getMutedTime));
            postUserVo.setMutedTime(mutedList.get(0).getMutedTime());
        }
        return ResponseResult.success(postUserVo);
    }

    @GetMapping("/queryPostUser/{postId}")
    public ResponseResult queryPostUser(@PathVariable String postId) {
        String userId=postsService.getObj(new LambdaQueryWrapper<Posts>().select(Posts::getUserId).eq(Posts::getId,postId),Object::toString);
        PostUserVo postUserVo = userMapper.getPostUserById(userId);
        setPostsUser(postUserVo);
        return ResponseResult.success(postUserVo);
    }

    @GetMapping("/queryUserPosts/{postId}")
    public ResponseResult queryUserPosts(@PathVariable String postId) {
        String userId=postsService.getObj(new LambdaQueryWrapper<Posts>().select(Posts::getUserId).eq(Posts::getId,postId),Object::toString);
        List<PostsSimpleVo> posts = userMapper.selectUserPost(userId);
        return ResponseResult.success(posts);
    }

    @GetMapping("/query/getAvatar/{username}")
    public ResponseResult getAvatar(@PathVariable String username) {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, username));
        Assert.notNull(user, "用户不存在");
        return ResponseResult.success(user.getAvatar(),null);
    }

    @GetMapping("/getUserNameExists/{username}")
    public ResponseResult getUserNameExists(@PathVariable String username) {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, username));
        if(Objects.isNull(user)){
            return ResponseResult.success(true,"用户名不存在");
        }else {
            return ResponseResult.success(false,"用户名已存在");
        }
    }

    @GetMapping("/queryUserIsMuted")
    public ResponseResult queryUserIsMuted(){
        return isMuted(SecurityUtils.getUserId().toString());
    }
    private ResponseResult isMuted(String userId){
        String muted = userService.getById(userId).getMuted();
        Map<String,Object> map=new HashMap<>();
        if ("0".equals(muted)){
            map.put("flag",false);
            return ResponseResult.success(map,"");
        }else if("1".equals(muted)){
            List<Muted> mutedList = mutedService.list(new LambdaQueryWrapper<Muted>()
                    .eq(Muted::getUserId, userId)
                    .ne(Muted::getEffective, 1)
                    .orderByDesc(Muted::getMutedTime));
            if (mutedList.size()==0){
                userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,0));
                map.put("flag",false);
                return ResponseResult.success(map,"");
            }
            for (int i = 1; i < mutedList.size(); i++) {
                mutedList.get(i).setEffective("1");
            }
            long mutedTime = mutedList.get(0).getMutedTime().getTime();
            long nowTime = new Date().getTime();
            if(mutedTime<nowTime){
                mutedList.get(0).setEffective("1");
                userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,0));
                map.put("flag",false);
                return ResponseResult.success(map,"");
            }else {
                map.put("flag",true);
                map.put("time",mutedList.get(0).getMutedTime().getTime());
                return ResponseResult.success(map,"你已被禁言至");
            }
        }else {
            map.put("flag",true);
            return ResponseResult.success(map,"你已被永久禁言");
        }
    }


    @GetMapping("/getUserDynamic/{userId}")
    public ResponseResult getUserDynamic(@RequestParam(defaultValue = "1") Integer currentPage,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @PathVariable String userId){
        Page<UserDynamic> postPage = new Page<>(currentPage, pageSize);
        IPage<UserDynamic> page = userMapper.selectUserDynamic(postPage, userId);
        List<UserDynamic> userDynamics = page.getRecords();
        for (UserDynamic userDynamic : userDynamics) {
            if (userDynamic.getIsPost().equals("0")){
                if(userDynamic.getTargetUserId()!=0){
                    userDynamic.setTargetUser(userMapper.getUserVoById(userDynamic.getTargetUserId()));
                    userDynamic.setParentContent(commentService.getObj(
                            new LambdaQueryWrapper<Comment>()
                                    .select(Comment::getContent)
                                    .eq(Comment::getId,userDynamic.getParentId()), Object::toString));
                }
            }
            userDynamic.setPost(postsMapper.selectPostsSimpleVo(userDynamic.getPostId().toString()));
        }
        return ResponseResult.success(page);
    }

    @PostMapping("/checkOldPass")
    public ResponseResult checkOldPass(@RequestBody Map<String,String> param){
        String encodedPassword = userMapper.getPassword(SecurityUtils.getUserId().toString());
        if (!SecurityUtils.matchesPassword(param.get("oldPass"),encodedPassword)) {
            return ResponseResult.success(false,"请输入正确的原密码");
        }else {
            return ResponseResult.success(true);
        }
    }

    @PostMapping("/updateAvatar")
    public ResponseResult updateAvatar(@RequestBody Map<String,String> param){
        userService.update(new LambdaUpdateWrapper<User>().set(User::getAvatar,param.get("avatar")).eq(User::getId,SecurityUtils.getUserId()));
        return ResponseResult.success("");
    }
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('user:user:edit')")
    public ResponseResult edit(@RequestBody User user) {
        user.setId(SecurityUtils.getUserId());
        user.setUpdateBy(SecurityUtils.getUsername());
        user.setUpdateTime(new Date());
        userService.updateById(user);
        return ResponseResult.success("");
    }

    @PostMapping("/checkEmailAndPhone")
    public ResponseResult checkEmailAndPhone(@RequestBody User user) {
        User updateUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, user.getUserName()));
        if (user.getEmail().equals(updateUser.getEmail())&&user.getPhonenumber().equals(updateUser.getPhonenumber())){
            return ResponseResult.success(true);
        }else {
            return ResponseResult.success(false);
        }
    }

    @Log(title = "用户密码修改", businessType = BusinessType.UPDATE)
    @PostMapping("/edit/forgotPassword")
    public ResponseResult forgotPassword(@RequestBody Map<String,String> param) {
        String encodePassword = SecurityUtils.encryptPassword(param.get("newPass"));
        String userName = param.get("userName");
        boolean update = userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getUserName, userName)
                .set(User::getPassword, encodePassword)
                .set(User::getPwdUpdateDate, new Date())
                .set(User::getUpdateBy, userName)
                .set(User::getUpdateTime, new Date()));
        return ResponseResult.success(true);
    }

    @Log(title = "用户密码修改", businessType = BusinessType.UPDATE)
    @PostMapping("/edit/pwd")
    @PreAuthorize("@dandelion.hasAuthority('user:user:edit')")
    public ResponseResult editPwd(@RequestBody Map<String,String> param) {
        String encodePassword = SecurityUtils.encryptPassword(param.get("newPass"));
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, SecurityUtils.getUserId())
                .set(User::getPassword, encodePassword)
                .set(User::getPwdUpdateDate, new Date())
                .set(User::getUpdateBy, SecurityUtils.getUsername())
                .set(User::getUpdateTime, new Date()));
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.deleteObject(loginUser.getUuid());
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @GetMapping("/getAuthentication")
    public ResponseResult getAuthentication(){
        Authentication authentication = authenticationService.getOne(new LambdaQueryWrapper<Authentication>().eq(Authentication::getUserId, SecurityUtils.getUserId()));
        return ResponseResult.success(authentication,"");
    }

    @PostMapping("/addAuthentication")
    @PreAuthorize("@dandelion.hasAuthority('user:authentication:add')")
    public ResponseResult addAuthentication(@RequestBody Authentication authentication){
        authentication.setCreateTime(new Date());
        authentication.setUserId(SecurityUtils.getUserId());
        authenticationService.save(authentication);
        return ResponseResult.success("");
    }

    @PostMapping("/editAuthentication")
    @PreAuthorize("@dandelion.hasAuthority('user:authentication:edit')")
    public ResponseResult editAuthentication(@RequestBody Authentication authentication){
        authentication.setPass("1");
        authentication.setUpdateBy(SecurityUtils.getUsername());
        authentication.setUpdateTime(new Date());
        authenticationService.updateById(authentication);
        return ResponseResult.success("");
    }

    private void setPostsUser(PostUserVo postUserVo){
        postUserVo.setPostNum(postsService.count(new LambdaQueryWrapper<Posts>().eq(Posts::getUserId,postUserVo.getId())));
        postUserVo.setElitePostNum(postsService.count(new LambdaQueryWrapper<Posts>().eq(Posts::getUserId,postUserVo.getId()).ne(Posts::getElite,0)));
        postUserVo.setCommentPostNum(commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId,postUserVo.getId())));
    }

}
