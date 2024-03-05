package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DuoSecurityMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
@Configuration(value = "DuoSecurityMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class DuoSecurityMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "duoSecurityBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ChainingMultifactorAuthenticationProviderBypassEvaluator duoSecurityBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("duoSecurityRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("duoSecurityPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("duoSecurityAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("duoSecurityCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("duoSecurityHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("duoSecurityGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("duoSecurityRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator duoSecurityRestMultifactorAuthenticationProviderBypass) {

        return BeanSupplier.of(ChainingMultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityPrincipalMultifactorAuthenticationProviderBypass);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityAuthenticationMultifactorAuthenticationProviderBypass);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityCredentialMultifactorAuthenticationProviderBypass);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityHttpRequestMultifactorAuthenticationProviderBypass);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityGroovyMultifactorAuthenticationProviderBypass);
                bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityRestMultifactorAuthenticationProviderBypass);
                return bypass;
            })
            .otherwise(() -> new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext))
            .get();
    }

    @ConditionalOnMissingBean(name = "duoSecurityRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.stream()
                    .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getRest().getUrl()))
                    .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                        new RestMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "duoSecurityGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.stream()
                    .filter(duo -> duo.getBypass().getGroovy().getLocation() != null)
                    .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                        new GroovyMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "duoSecurityHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.stream().filter(duo -> {
                    val props = duo.getBypass();
                    return StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
                }).forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                    new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "duoSecurityCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.stream()
                    .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getCredentialClassType()))
                    .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                        new CredentialMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "duoSecurityRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                    new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                    new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(duo.getId(), applicationContext)));
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.stream()
                    .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getPrincipalAttributeName()))
                    .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                        new PrincipalMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val duoProps = casProperties.getAuthn().getMfa().getDuo();
                val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
                duoProps.stream().filter(duo -> {
                    val props = duo.getBypass();
                    return StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
                           || StringUtils.isNotBlank(props.getAuthenticationMethodName());
                }).forEach(
                    duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(
                        new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId(), applicationContext)));
                if (bypass.isEmpty()) {
                    return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
                }
                return bypass;
            })
            .otherwiseProxy()
            .get();
    }
}
