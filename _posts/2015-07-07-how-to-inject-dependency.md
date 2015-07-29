---
layout:     post
title:      依赖注入的一般方法
date:       2015-07-07 14:41
#summary:    
categories: tech
---

在项目没有引入Spring进行依赖注入的时候，为了降低耦合性、提高可测性，可以使用以下方法提供依赖注入：

* 构造器依赖注入
* Setter依赖注入
* Override依赖置换

使用优先顺序从高到低。

### 构造器依赖注入

构造器依赖注入，是在提供普通构造器之外，再提供一个用来依赖注入的构造，将依赖的对象注入实例。
依赖注入构造器只能被两类代码使用：

* 普通构造器
* 测试用例代码

普通构造器通过依赖注入构造器将实际依赖对象注入到实例对象中。
测试用例代码将mock对象通过依赖注入构造器注入产生实例对象，用来进行隔离测试。

示例代码：

```Java
public class DAPBigDataServiceComponent
{

    private IManagerInternalProxy internalProxy;
    private ISystemConfigProvider provider;
    private DapServerI18N dapServerI18N;
    private ServiceManagerProxy serviceManagerProxy;
    private HostManagerProxy hostManagerProxy;

    @VisibleForTesting
    protected DAPBigDataServiceComponent(IManagerInternalProxy managerInternalProxy,
        ISystemConfigProvider systemConfigProvider, ServiceManagerProxy serviceManagerProxy,
        HostManagerProxy hostManagerProxy, DapServerI18N dapServerI18N)
    {
        this.internalProxy = managerInternalProxy;
        this.provider = systemConfigProvider;
        this.serviceManagerProxy = serviceManagerProxy;
        this.hostManagerProxy = hostManagerProxy;
        this.dapServerI18N = dapServerI18N;
    }

    public DAPBigDataServiceComponent()
    {
        this(new ManagerInternalProxy(
                new DAPManagerSystemConfigProvider( new DAPManagerFileSystemProvider(),
                    new JCADBConnectionService())),
            new DAPManagerSystemConfigProvider(new DAPManagerFileSystemProvider(),
                new JCADBConnectionService()), new ServiceManagerProxy(), new HostManagerProxy(),
            DapServerI18N.getInstance());
    }
     ......
    
}
```

测试用例：

```
@RunWith(MockitoJUnitRunner.class)
public class DAPBigDataServiceComponentTest
{
    private DAPBigDataServiceComponent component;
    @Mock
    private IManagerInternalProxy managerInternalProxy;
    @Mock
    private ISystemConfigProvider systemConfigProvider;
    @Mock
    private ServiceManagerProxy serviceManagerProxy;
    @Mock
    private HostManagerProxy hostManagerProxy;
    @Mock
    private DapServerI18N dapServerI18N;

    @Before
    public void setUp() throws Exception
    {
        // 使用依赖注入构造器注入mock对象
        component = new DAPBigDataServiceComponent(managerInternalProxy, systemConfigProvider ,
            serviceManagerProxy, hostManagerProxy, dapServerI18N);

        // 定义mock对象的行为
        when( serviceManagerProxy.getDeploymentedService("1", ServiceEnum.SPARKSQL , "1"))
            .thenReturn(mockSparkSQLDeployments());
        .......
    }
    ......
 } 
```

构造器依赖注入的好处：

* 利用构造器注入依赖，能够保证生成对象的完整性（相对于setter依赖注入，能够避免漏注入依赖）
* 生产运行环境中的依赖关系完全被封装在普通构造器内

构造器依赖注入使用要点：

* 依赖注入构造器建议使用特殊的标注进行标记，比如@VisibleForTesting，以示区分
* 依赖注入构造器使用package-local或protected可见性，以降低生产代码误调用的可能性
* 普通构造器必须通过依赖注入构造器将生产环境中的依赖关系注入（普通构造器无法被测试，因此需要普通构造尽可能的简单而不含有其他逻辑，因此，需要普通构造器将依赖注入委托给依赖注入构造器，以保证依赖注入的正确性 ）
* 除了普通构造器之外，其他生产代码不应调用依赖注入构造器（依赖注入构造器只为测试而生）

依赖构造工厂（此场景较少出现）：
由于依赖注入构造器只在package内可见，当不在同一个package内的测试代码想使用依赖注入构造器，需要在测试代码目录的同package中生成一个public可见性的依赖构造工厂，以便其他package的测试代码使用。

示例代码：

```
public class DAPBigDataServiceComponentFactory
{
    public DAPBigDataServiceComponent createDAPBigDataServiceComponent(
        IManagerInternalProxy managerInternalProxy, ISystemConfigProvider systemConfigProvider,
        ServiceManagerProxy serviceManagerProxy, HostManagerProxy hostManagerProxy,
        DapServerI18N dapServerI18N)
    {
        return new DAPBigDataServiceComponent(managerInternalProxy, systemConfigProvider,
            serviceManagerProxy, hostManagerProxy, dapServerI18N);
    }
}
```

（以上代码在测试代码目录中，仅供测试代码使用。）

构造器依赖注入不适用的场景：
// TBD

### Setter依赖注入

Setter依赖注入，是提供setter方法对依赖进行注入。

示例代码：
public class DAPBigDataServiceComponent
{
    private static final DebugPrn LOGGER = new DebugPrn(DAPBigDataServiceComponent.class.getName());
    protected IManagerInternalProxy internalProxy;
    protected ISystemConfigProvider provider;
    private DapServerI18NProxy dapServerI18NProxy;
    private ServiceManagerProxy serviceManagerProxy;
    private HostManagerProxy hostManagerProxy;

    public void setDapServerI18NProxy(DapServerI18NProxy dapServerI18NProxy)
    {
        this.dapServerI18NProxy = dapServerI18NProxy;
    }

    public void setServiceManagerProxy(ServiceManagerProxy serviceManagerProxy)
    {
        this.serviceManagerProxy = serviceManagerProxy;
    }

    public void setHostManagerProxy(HostManagerProxy hostManagerProxy)
    {
        this.hostManagerProxy = hostManagerProxy;
    }
    ......
}

测试用例：
@RunWith(MockitoJUnitRunner.class) 
public class DAPBigDataServiceComponentTest
{
    private DAPBigDataServiceComponent component;
    @Mock
    private IManagerInternalProxy managerInternalProxy;
    @Mock
    private ISystemConfigProvider systemConfigProvider;
    @Mock
    private ServiceManagerProxy serviceManagerProxy;
    @Mock
    private HostManagerProxy hostManagerProxy;
    @Mock
    private DapServerI18NProxy dapServerI18NProxy;

    @Before
    public void setUp() throws Exception
    {
        component = new DAPBigDataServiceComponent(); 
        // 使用依赖注入构造器注入mock对象 
 component.setServiceManagerProxy(serviceManagerProxy);
        component.setHostManagerProxy(hostManagerProxy);
        component.setDapServerI18NProxy(dapServerI18NProxy);

        // 定义mock对象的行为 
when(serviceManagerProxy.getDeploymentedService("1", ServiceEnum.SPARKSQL, "1" ))
            .thenReturn(mockSparkSQLDeployments());
        ....... 
    }
    ......
}

Setter依赖注入的缺点：

* 没有一个集中进行依赖注入的机制，依赖注入可以散落在代码各处
* 同时，相比于构造器依赖注入，编译器无法保证通过Setter依赖注入的对象其依赖注入具有完整性（比如，我对某个对象进行依赖注入，调用了3个依赖注入setter，但是我无法很容易的确认，这个对象就这3个依赖需要注入，再没有其他依赖进行注入）


其他依赖注入方法不适用，需要使用Setter依赖注入的场景：
// TBD

###　Override依赖置换

Override依赖置换，是将依赖封装在对象方法中，由对象方法提供依赖对象的获取。当需要置换依赖对象时，通过Override该依赖提供方法进行依赖置换。

代码示例：
public class InstallXmlValidator
{
    ......
    protected String getDapManagerIp()
    {
        return Util.getDapManagerIp();
    }

    protected String getI18nValue(String labelKey)
    {
        return DapServerI18N.getInstance().getLabel(labelKey);

    }

    protected String getI18nValue(String labelKey, String[] params)
    {
        return DapServerI18N.getInstance().getLabel(labelKey, params);

    }
}

测试用例：
public class InstallXmlValidatorTest
{
    private InstallXmlValidator validator;
    private Map<String, String> i18nInfo = new HashMap<String, String>();

     @Before
    public void setUp() throws Exception
    {
        validator = new InstallXmlValidator()
        {
            @Override
            protected String getI18nValue(String labelKey)
            {
                return i18nInfo.get(labelKey);
            }

            @Override
            protected String getI18nValue(String labelKey, String[] params)
            {
                return MessageFormat.format(i18nInfo.get(labelKey), params);
            }

            @Override
            protected String getDapManagerIp()
            {
                return "127.0.0.1";
            }
        };
        initI18nInfo();
    }
    ......
}

Override依赖置换的缺点：
同Setter依赖注入的缺点。

其他依赖注入方法不适用，需要使用Override依赖置换的场景：
// TBD



