package com.dhlyf.walletmodel.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 国际化语言包
 * ISO 639-1 语言规范
 * ISO 3166-1 国家规范
 * Created by zhongjingyun on 2017/11/3.
 */
public enum Language {
    Chinese("zh", 1, "zh_CN", "cnName", 1, 1, "简体中文", "4,1", "{familyName}{givenName}", "中文", "￥", "CNY", 2,"+86"),
    English("en", 2, "en_US", "enName", 1, 1, "English", "5,2", "{givenName}{familyName}", "英文", "$", "USD", 2,"+1"),
    Korean("ko", 3, "ko_KR", "koName", 1, 1, "한국어", "6,3", "{familyName}{givenName}", "韩文", "₩", "KRW", 0,"+82"),
    Traditional_Chinese("el", 4, "el_GR", "cnName", 1, 1, "繁体中文", "8,7", "{familyName}{givenName}", "繁体中文", "￥", "CNY", 2,"+86"),
    Mongolian("mn", 5, "mn_MN", "mnName", 1, 1, "Монгол хэл", "10,9", "{familyName}{givenName}", "蒙文", "₮", "MNT", 2,"+976"),
    Russian("ru", 6, "ru_RU", "ruName", 1, 1, "русский язык", "12,11", "{familyName}{givenName}", "俄语", "₽", "RUB", 2,"+7"),
    Japanese("ja", 7, "ja_JP", "jaName", 1, 1, "日本語", "14,13", "{familyName}{givenName}", "日语", "¥", "JPY", 2,"+81"),;

    public String lang;//语言编码
    public int langTypeId;//多语言id（这个字段给cms系统使用）
    public String langType;//多语言和国家编码
    public String phoneCountryName;//电话号码国家编码名字
    public int status;//开启状态 1开启 0关闭
    public int operateOpen;//后台语言的开启或者关闭 1开启 0关闭
    public String showName;//页面展示名字
    public String cmsTypeId;//后台cms系统分类id
    public String nameOrder;//不同国家姓名顺序，familyName姓，givenName名
    public String description;
    public String moneySymbol;//货币符号
    public String countryCoin;//国家法定货币
    public int coinPrecision;//币种显示精度
    public String countryCode;//国家代码

    private Language(String lang, int langTypeId, String langType, String phoneCountryName, int status, int operateOpen,
                     String showName, String cmsTypeId, String nameOrder, String description, String moneySymbol,
                     String countryCoin, String countryCode) {
        this.lang = lang;
        this.langTypeId = langTypeId;
        this.langType = langType;
        this.phoneCountryName = phoneCountryName;
        this.status = status;
        this.operateOpen = operateOpen;
        this.showName = showName;
        this.cmsTypeId = cmsTypeId;
        this.nameOrder = nameOrder;
        this.description = description;
        this.moneySymbol = moneySymbol;
        this.countryCoin = countryCoin;
        this.countryCode = countryCode;
    }


    private Language(String lang, int langTypeId, String langType, String phoneCountryName, int status, int operateOpen,
                     String showName, String cmsTypeId, String nameOrder, String description, String moneySymbol,
                     String countryCoin, int coinPrecision, String countryCode) {
        this.lang = lang;
        this.langTypeId = langTypeId;
        this.langType = langType;
        this.phoneCountryName = phoneCountryName;
        this.status = status;
        this.operateOpen = operateOpen;
        this.showName = showName;
        this.cmsTypeId = cmsTypeId;
        this.nameOrder = nameOrder;
        this.description = description;
        this.moneySymbol = moneySymbol;
        this.countryCoin = countryCoin;
        this.coinPrecision = coinPrecision;
        this.countryCode = countryCode;
    }


    public String getPhoneCountryName() {
        return phoneCountryName;
    }


    public void setPhoneCountryName(String phoneCountryName) {
        this.phoneCountryName = phoneCountryName;
    }


    public String getCmsTypeId() {
        return cmsTypeId;
    }

    public void setCmsTypeId(String cmsTypeId) {
        this.cmsTypeId = cmsTypeId;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public int getLangTypeId() {
        return langTypeId;
    }

    public void setLangTypeId(int langTypeId) {
        this.langTypeId = langTypeId;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOperateOpen() {
        return operateOpen;
    }

    public void setOperateOpen(int operateOpen) {
        this.operateOpen = operateOpen;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLangType() {
        return langType;
    }

    public void setLangType(String langType) {
        this.langType = langType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public static Language fromValue(String lang) {
        for (Language t : Language.values()) {
            if (t.lang.equalsIgnoreCase(lang)) {
                return t;
            }
        }
        return null;
    }

    /**
     * 根据国家法币获取语言
     *
     * @param countryCoin
     * @return
     */
    public static Language fromCountryCoin(String countryCoin) {
        for (Language t : Language.values()) {
            if (t.countryCoin.equalsIgnoreCase(countryCoin)) {
                return t;
            }
        }
        return English;
    }

    public static Language fromLangType(String langType) {
        for (Language t : Language.values()) {
            if (t.langType.equalsIgnoreCase(langType)) {
                return t;
            }
        }
        return Language.English;
    }

    //根据langTypeId获取枚举
    public static Language fromLangTypeId(int langTypeId) {
        for (Language t : Language.values()) {
            if (t.langTypeId == langTypeId) {
                return t;
            }
        }
        return null;
    }

    //获取已开通的语言
    public static List<Language> getStartLanguage() {
        ArrayList<Language> langList = new ArrayList<Language>();
        for (Language t : Language.values()) {
            if (t.status == 1) {
                langList.add(t);
            }
        }
        return langList;
    }

    //获取后台已开通的语言
    public static List<Language> getOperateStartLanguage() {
        ArrayList<Language> langList = new ArrayList<Language>();
        Arrays.asList(Language.values()).forEach(
                language -> {
                    if (language.operateOpen == 1) {
                        langList.add(language);
                    }
                }
        );


        return langList;
    }

    public static Language fromName(String name) {
        for (Language t : Language.values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static Language getByCountryCod(String countryCode) {
        for (Language t : Language.values()) {
            if (t.countryCode.equals(countryCode)) {
                return t;
            }
        }
        return English;
    }


}
