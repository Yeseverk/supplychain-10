package com.lyf.supplychain.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Document;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 网关路由配置测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class GatewayRouteConfigTest {

    @Test
    void localGatewayConfigShouldImportRemoteGatewayConfig() {
        Properties properties = loadYaml(projectRoot().resolve("supplychain-gateway/src/main/resources/application.yml"));

        assertThat(properties.getProperty("spring.config.import[1]"))
                .isEqualTo("optional:nacos:supplychain-gateway.yml?group=SUPPLYCHAIN_GROUP");
    }

    @Test
    void remoteGatewayConfigShouldRouteSystemPathByLoadBalancer() {
        Properties properties = loadYaml(projectRoot().resolve("nacos-config/extension/supplychain-gateway.yml"));

        assertThat(properties.getProperty("spring.cloud.gateway.routes[0].predicates[0]"))
                .isEqualTo("Path=/system/**");
        assertThat(properties.getProperty("spring.cloud.gateway.routes[0].uri"))
                .isEqualTo("${supplychain.gateway.system-uri:lb://supplychain-system}");
    }

    @Test
    void remoteGatewayConfigShouldRouteWmsApiAndLegacyPaths() {
        Properties properties = loadYaml(projectRoot().resolve("nacos-config/extension/supplychain-gateway.yml"));

        assertThat(properties).containsEntry(
                "spring.cloud.gateway.routes[6].predicates[0]",
                "Path=/api/wms/**"
        );
        assertThat(properties).containsEntry(
                "spring.cloud.gateway.routes[7].predicates[0]",
                "Path=/wms/**"
        );
        assertThat(properties.getProperty("spring.cloud.gateway.routes[7].uri"))
                .isEqualTo("${supplychain.gateway.warehouse-uri:lb://supplychain-warehouse}");
    }

    @Test
    void gatewayPomShouldDeclareLoadBalancerStarterForLbRoute() throws Exception {
        assertThat(hasDependency(
                projectRoot().resolve("supplychain-gateway/pom.xml"),
                "org.springframework.cloud",
                "spring-cloud-starter-loadbalancer"
        )).isTrue();
    }

    private Properties loadYaml(Path path) {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new FileSystemResource(path));
        Properties properties = factoryBean.getObject();
        return properties == null ? new Properties() : properties;
    }

    private Path projectRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("nacos-config"))) {
            path = path.getParent();
        }
        return path;
    }

    private boolean hasDependency(Path pomPath, String groupId, String artifactId) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(pomPath.toFile());
        String expression = "//*[local-name()='dependency']"
                + "[*[local-name()='groupId']='" + groupId + "'"
                + " and *[local-name()='artifactId']='" + artifactId + "']";
        return ((Double) XPathFactory.newInstance()
                .newXPath()
                .evaluate("count(" + expression + ")", document, XPathConstants.NUMBER)) > 0;
    }
}
