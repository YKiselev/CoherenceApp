package org.uze.spring;

import com.google.common.base.Preconditions;
import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.builder.ParameterizedBuilder;
import com.tangosol.config.ConfigurationException;
import com.tangosol.config.expression.ParameterResolver;
import com.tangosol.config.xml.AbstractNamespaceHandler;
import com.tangosol.config.xml.ElementProcessor;
import com.tangosol.config.xml.ProcessingContext;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.ResourceRegistry;
import org.springframework.beans.factory.BeanFactory;

/**
 * Created by Uze on 23.08.2014.
 */
public class SpringNamespaceHandler extends AbstractNamespaceHandler {

    public SpringNamespaceHandler() {
        registerProcessor("bean", new ElementProcessor<Object>() {
            @Override
            public Object process(ProcessingContext context, XmlElement element) throws ConfigurationException {
                final String beanName = element.getString();
                SpringBeanBuilder bldr = new SpringBeanBuilder(beanName, context.getResourceRegistry());

                context.inject(bldr, element);

//                SimpleParameterList listParam = new SimpleParameterList();
//                String              sPrefix   = element.getQualifiedName().getPrefix();
//
//                for (XmlElement e : (List<XmlElement>) element.getElementList())
//                {
//                    QualifiedName qName = e.getQualifiedName();
//
//                    if (Base.equals(sPrefix, qName.getPrefix()) && qName.getLocalName().equals("property"))
//                    {
//                        listParam.add(context.processElement(e));
//                    }
//                }
//
//                bldr.setParameterList(listParam);

                return bldr;
            }
        });
    }

    public static class SpringBeanBuilder implements ParameterizedBuilder<Object> {

        private final String beanName;
        private final ResourceRegistry resourceRegistry;

        public SpringBeanBuilder(String beanName, ResourceRegistry resourceRegistry) {
            this.beanName = beanName;
            this.resourceRegistry = resourceRegistry;
        }

        @Override
        public Object realize(ParameterResolver parameterResolver, ClassLoader classLoader, ParameterList parameters) {
            final BeanFactory beanFactory = resourceRegistry.getResource(BeanFactory.class);
            Preconditions.checkNotNull(beanFactory);

            return beanFactory.getBean(beanName);
            //String        sBeanName      = getBeanName().evaluate(parameterResolver);
            //Object        oBean          = ensureBeanFactory(resolver, loader).getBean(sBeanName);
            //ParameterList listPropParams = listParameters == null ? m_listParameters : listParameters;

//            if (listPropParams != null)
//            {
//                for (Parameter param : listPropParams)
//                {
//                    Object oValue    = param.evaluate(resolver).get();
//                    String sProperty = param.getName();
//
//                    if (sProperty == null || sProperty.isEmpty())
//                    {
//                        throw new ConfigurationException("Property element missing \"name\" attribute",
//                            "Ensure that bean property elements have a \"name\" attribute "
//                                + "(i.e. <property name=\"name\"> ");
//                    }
//
//                    String sMethod = "set" + Character.toUpperCase(sProperty.charAt(0)) + sProperty.substring(1);
//
//                    try
//                    {
//                        ClassHelper.invoke(oBean, sMethod, new Object[] {oValue});
//                    }
//                    catch (Exception e)
//                    {
//                        throw new ConfigurationException(String.format("Could not invoke '%s' on bean '%s'", sMethod,
//                            sBeanName),
//                            String
//                                .format("Ensure that property '%s' contains a 'set' method on bean '%s'",
//                                    sProperty, sBeanName),
//                            e);
//                    }
//                }
//            }
//            return null; // oBean
        }
    }
}
