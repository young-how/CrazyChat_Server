package com.dlut.crazychat.game;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
@Data
@Component
public class dailySign {
    @Resource
    RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${game.daiySign.base_reward}")
    private int base_reward;   //基础签到奖励
    @Value("${game.daiySign.factor_sign_count}")
    private int factor_sign_count;  //签到总数的奖励递增系数
    @Value("${game.daiySign.factor_sign_continues}")
    private int factor_sign_continues;  //连续签到的奖励递增系数
    @Value("${game.daiySign.max_magnification}")
    private double max_magnification;  //最高翻倍倍率

    /**
     * 用户签到，可以补签
     * @param userId 用户ID
     * @param dateStr 查询的日期，默认当天 yyyy-MM-dd
     * @return 连续签到次数和总签到次数
     * */
    public Map<String, Object> doSign(String userId, String dateStr){
        //获取当前用户登录信息
        Map<String, Object> result = new HashMap<>();
        //获取日期
        Date date = getDate(dateStr);
        //获取日期对应的天数，多少号  偏移量
        int day = dayOfMonth(date) -1;
        //构建redis key
        String signKey = buildSignKey(userId,date);
        //查看指定日期是否已签到
        if (isSigned(signKey, day)){
            result.put("message", "当前日期已完成签到，无需再签");
            result.put("code",400);
            // 根据当前日期统计签到次数
            Date today = new Date();
            //统计连续签到次数
            int continuous =  getSignCount(userId, today);
            //统计总签到次数
            long count = getSumSignCount(userId, today);
            result.put("continuous",continuous);
            result.put("count",count);
            return result;
        }
        // 签到
        redisTemplate.opsForValue().setBit(signKey, day, true);
        // 根据当前日期统计签到次数
        Date today = new Date();
        //统计连续签到次数
        int continuous =  getSignCount(userId, today);
        //统计总签到次数
        long count = getSumSignCount(userId, today);
        int reward=calReward(count,continuous);   //生成的随机奖励
        result.put("message","签到成功");
        result.put("code",200);
        result.put("continuous",continuous);
        result.put("count",count);
        result.put("reward",reward);
        return result;
    }
    /**
     * 生成奖励值
     * @param count 签到总数
     * @param continuous 连续签到的天数
     * @return 签到奖励
     * */
    public int calReward(long count,int continuous){
        //计算奖励值
        Random rand=new Random();  //随机数生成器
        double rand_num= rand.nextDouble();  //随机生成的小数
        return (int)((base_reward+factor_sign_count*count+factor_sign_continues*continuous)/(rand_num+(1.0/max_magnification)));   //生成的随机奖励;
    }
    /**
     *
     * 格式化日期
     * @param StrDate
     * @return
     * */
    private Date parseDate(String StrDate){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = null;
        try{
            myDate = dateFormat.parse(StrDate);
        }catch (ParseException e){
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        return  myDate;
    }

    private String format(Date date, String format){
        DateFormat dateFormat = new SimpleDateFormat(format);
        String myDate = dateFormat.format(date);
        return myDate;
    }
    /**
     * 获取用户当前的时间
     * @param dateStr yyyy-MM-dd
     * @return
     * */
    private Date getDate(String dateStr) {
        return Objects.isNull(dateStr) ? new Date() : parseDate(dateStr);
    }

    /**
     * 根据日期获取日期所在月份的天数
     * @param date
     * @return
     * */
    private int dayOfMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }


    /**
     * 构建Redis key userId:yyyyMM
     * @param userId 用户ID
     * @param date 日期
     * @return
     * */
    private String buildSignKey(String userId, Date date){
        return String.format("img2d_user_daily_sign:%s:%s",userId,format(date,"yyyyMM"));
    }

    /**
     * 统计连续签到次数
     * 如今天16号 无符号 查询16个bit
     * @param userId 用户ID
     * @param date 查询日期
     * @return
     * */
    private int getSignCount(String userId, Date date){
        int dayOfMonth = dayOfMonth(date);
        // 构建 Redis key
        String signKey = buildSignKey(userId,date);
        // 获取日期对应的天数 多少号
        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                .valueAt(0);
        //获取用户从当前日期开始到1号的所有签到状态
        List<Long> signList = stringRedisTemplate.opsForValue().bitField(signKey,bitFieldSubCommands);
        if (signList == null || signList.isEmpty()){
            return 0;
        }
        //连续签到计数器
        int signCount = 0;
        long v = signList.get(0) == null ? 0 : signList.get(0);
        //位移计算连续签到次数
        for (int i = dayOfMonth; i > 0; i--){  //i表示位移操作次数
            //右移再左移，如果等于自己说明最低位是0 表示未签到
            if (v >> 1 << 1 == v){
                // 如果为0 表示未签到 判断是否为当前
                if (i != dayOfMonth) break;
            } else {
                // 右移再左移， 如果不等于自己，说明最低位是1 表示签到
                signCount++;
            }
            //右移一位并重新赋值，相当于把最低位丢弃一位然后重新计算
            v >>= 1;
        }
        return signCount;
    }

    /**
     * 统计总签到次数
     * @param userId 用户ID
     * @param date 查询的日期
     * */
    private Long getSumSignCount(String userId, Date date){
        //构建Redis Key
        String signKey = buildSignKey(userId, date);
        //e.g BITCOUNT user:sign:5:202306
        return (Long) redisTemplate.execute(
                (RedisCallback<Long>) con -> con.bitCount(signKey.getBytes())
        );
    }

    /**
     * 统计月份签到次数
     * @param userId 用户ID
     * @param dateStr 用户日期
     * */
    public String monthSigned(String userId, String dateStr){
        //获取日期
        Date date = getDate(dateStr);
        String signKey = buildSignKey(userId, date);
        //获取日期对应的天数， 多少号，
        int dayOfMonth = dayOfMonth(date);
        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                .valueAt(0);
        // 获取月份的所有签到状态
        List<Long> list = redisTemplate.opsForValue().bitField(signKey, bitFieldSubCommands);
        String total = Long.toBinaryString(list.get(0));
        return total;
    }

    /**
     * 判断用户是否已经签到
     * @param userId   用户ID String
     * @param offset     用户日期
     * */
    private Boolean isSigned(String userId, int offset ){
        //偏移量 offset 从 0 开始
        return redisTemplate.opsForValue().getBit(userId, offset);
    }

}
