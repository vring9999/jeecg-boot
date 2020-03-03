package org.jeecg.modules.demo.orerInfo9.service;

import org.jeecg.modules.demo.orerInfo9.entity.TestOrderProduct;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 订单产品明细
 * @Author: jeecg-boot
 * @Date:   2020-02-21
 * @Version: V1.0
 */
public interface ITestOrderProductService extends IService<TestOrderProduct> {

	public List<TestOrderProduct> selectByMainId(String mainId);
}
