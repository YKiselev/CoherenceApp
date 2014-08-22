package org.uze.spring;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.ClassHelper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Iterator;

public class SpringAwareCacheFactory extends DefaultConfigurableCacheFactory implements BeanFactoryAware {

    public SpringAwareCacheFactory() {
    }

    /**
     * Construct a SpringAwareCacheFactory using the specified path to
     * a "cache-config.dtd" compliant configuration file or resource.  This
     * will also create a Spring ApplicationContext based on the supplied
     * path to a Spring compliant configuration file or resource.
     *
     * @param sCacheConfig location of a cache configuration
     * @param sAppContext  location of a Spring application context
     */
//    public SpringAwareCacheFactory(String sCacheConfig, String sAppContext) {
//        super(DependenciesHelper.newInstance(sCacheConfig));
//
//        azzert(sAppContext != null && sAppContext.length() > 0,
//                "Application context location required");
//
//        m_beanFactory = sCacheConfig.startsWith("file:") ? (BeanFactory)
//                new FileSystemXmlApplicationContext(sAppContext) :
//                new ClassPathXmlApplicationContext(sAppContext);
//
//        // register a shutdown hook so the bean factory cleans up
//        // upon JVM exit
//        ((AbstractApplicationContext) m_beanFactory).registerShutdownHook();
//    }

    /**
     * Construct a SpringAwareCacheFactory using the specified path to
     * a "cache-config.dtd" compliant configuration file or resource and
     * the supplied Spring BeanFactory.
     *
     * @param sPath       the configuration resource name or file path
     * @param beanFactory Spring BeanFactory used to load Spring beans
     */
//    public SpringAwareCacheFactory(String sPath, BeanFactory beanFactory) {
//        super(sPath);
//
//        m_beanFactory = beanFactory;
//    }


    /**
     * Create an Object using the "class-scheme" element.
     * <p/>
     * In addition to the functionality provided by the super class,
     * this will retreive an object from the configured Spring BeanFactory
     * for class names that use the following format:
     * <p/>
     * &lt;class-name&gt;spring-bean:sampleCacheStore&lt;/class-name&gt;
     * <p/>
     * <p/>
     * Parameters may be passed to these beans through setter injection as well:
     * <p/>
     * &lt;init-params&gt;
     * &lt;init-param&gt;
     * &lt;param-name&gt;setEntityName&lt;/param-name&gt;
     * &lt;param-value&gt;{cache-name}&lt;/param-value&gt;
     * &lt;/init-param&gt;
     * &lt;/init-params&gt;
     * <p/>
     * <p/>
     * Note that Coherence will manage the lifecycle of the instantiated Spring
     * bean, therefore any beans that are retrieved using this method should be
     * scoped as a prototype in the Spring configuration file, for example:
     * <p/>
     * &lt;bean id="sampleCacheStore"
     * class="com.company.SampleCacheStore"
     * scope="prototype"/&gt;
     *
     * @param info     the cache info
     * @param xmlClass "class-scheme" element.
     * @param context  BackingMapManagerContext to be used
     * @param loader   the ClassLoader to instantiate necessary classes
     * @return a newly instantiated Object
     * @see DefaultConfigurableCacheFactory#instantiateAny(
     *CacheInfo, XmlElement, BackingMapManagerContext, ClassLoader)
     */
    public Object instantiateAny(CacheInfo info, XmlElement xmlClass,
                                 BackingMapManagerContext context, ClassLoader loader) {
        if (translateSchemeType(xmlClass.getName()) != SCHEME_CLASS) {
            throw new IllegalArgumentException(
                    "Invalid class definition: " + xmlClass);
        }

        String sClass = xmlClass.getSafeElement("class-name").getString();

        if (sClass.startsWith(SPRING_BEAN_PREFIX)) {
            String sBeanName = sClass.substring(SPRING_BEAN_PREFIX.length());

            azzert(sBeanName != null && sBeanName.length() > 0,
                    "Bean name required");

            XmlElement xmlParams = xmlClass.getElement("init-params");
            XmlElement xmlConfig = null;
            if (xmlParams != null) {
                xmlConfig = new SimpleElement("config");
                XmlHelper.transformInitParams(xmlConfig, xmlParams);
            }

            Object oBean = getBeanFactory().getBean(sBeanName);

            if (xmlConfig != null) {
                for (Iterator iter = xmlConfig.getElementList().iterator(); iter.hasNext(); ) {
                    XmlElement xmlElement = (XmlElement) iter.next();

                    String sMethod = xmlElement.getName();
                    String sParam = xmlElement.getString();
                    try {
                        ClassHelper.invoke(oBean, sMethod, new Object[]{sParam});
                    } catch (Exception e) {
                        ensureRuntimeException(e, "Could not invoke " + sMethod +
                                "(" + sParam + ") on bean " + oBean);
                    }
                }
            }
            return oBean;
        } else {
            return super.instantiateAny(info, xmlClass, context, loader);
        }
    }

    @Override
    public NamedCache ensureCache(String sCacheName, ClassLoader loader) {
        return super.ensureCache(sCacheName, loader);
    }

    /**
     * Get the Spring BeanFactory used by this CacheFactory.
     *
     * @return the Spring {@link BeanFactory} used by this CacheFactory
     */
    public BeanFactory getBeanFactory() {
        azzert(m_beanFactory != null, "Spring BeanFactory == null");
        return m_beanFactory;
    }

    /**
     * Set the Spring BeanFactory used by this CacheFactory.
     *
     * @param beanFactory the Spring {@link BeanFactory} used by this CacheFactory
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }


    private BeanFactory m_beanFactory;

    /**
     * Prefix used in cache configuration "class-name" element to indicate
     * this bean is in Spring.
     */
    private static final String SPRING_BEAN_PREFIX = "spring-bean:";
}

