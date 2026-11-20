package com.linli.supermarket.controller;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.entity.PointsRecord;
import com.linli.supermarket.service.AuthService;
import com.linli.supermarket.service.PointsService;
import com.linli.supermarket.util.UserContext;
import com.linli.supermarket.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 积分与会员资料接口。 */
@RestController
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private AuthService authService;

    /** 积分明细。 */
    @GetMapping("/api/points/records")
    public Result<PageResult<PointsRecord>> records(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.ok(pointsService.records(UserContext.getUserId(), current, size));
    }

    /** 会员资料。 */
    @GetMapping("/api/member/profile")
    public Result<UserVO> profile() {
        return Result.ok(authService.me(UserContext.getUserId()));
    }
}
