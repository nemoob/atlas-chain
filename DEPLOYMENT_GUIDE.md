# Atlas-Chain 部署指南

## 发布到 Maven 中央仓库

本指南将帮助您将 Atlas-Chain 项目发布到 Maven 中央仓库。

### 前置条件

1. **Sonatype OSSRH 账户**
   - 在 [Sonatype JIRA](https://issues.sonatype.org) 创建账户
   - 创建一个新的项目票据来申请 `io.github.nemoob` 组ID
   - 等待审核通过

2. **GPG 密钥**
   ```bash
   # 生成 GPG 密钥
   gpg --gen-key
   
   # 列出密钥
   gpg --list-keys
   
   # 发布公钥到密钥服务器
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **Maven 配置**
   在 `~/.m2/settings.xml` 中添加服务器配置：
   ```xml
   <settings>
     <servers>
       <server>
         <id>ossrh</id>
         <username>your-jira-username</username>
         <password>your-jira-password</password>
       </server>
     </servers>
   </settings>
   ```

### 发布步骤

#### 1. 准备发布版本

```bash
# 确保代码已提交
git add .
git commit -m "Prepare for release"
git push

# 创建发布标签
git tag -a v0.0.1 -m "Release version 0.0.1"
git push origin v0.0.1
```

#### 2. 执行发布

```bash
# 清理并编译
mvn clean compile

# 运行测试
mvn test

# 使用 release profile 进行部署
mvn clean deploy -P release
```

#### 3. 验证发布

1. 登录 [Nexus Repository Manager](https://s01.oss.sonatype.org/)
2. 在 "Staging Repositories" 中找到您的发布
3. 检查内容并点击 "Close"
4. 等待验证完成后点击 "Release"

### 发布命令详解

#### 快照版本发布
```bash
# 发布快照版本（版本号包含 -SNAPSHOT）
mvn clean deploy
```

#### 正式版本发布
```bash
# 发布正式版本（需要 GPG 签名）
mvn clean deploy -P release
```

#### 仅构建不发布
```bash
# 仅构建所有 artifacts
mvn clean package
```

### 配置说明

#### POM 配置要素

项目 POM 已包含以下 Maven 中央仓库必需的配置：

1. **基本信息**
   - `name`: 项目名称
   - `description`: 项目描述
   - `url`: 项目主页

2. **许可证信息**
   - MIT License

3. **开发者信息**
   - 开发者ID、姓名、邮箱等

4. **SCM 信息**
   - Git 仓库连接信息

5. **分发管理**
   - Sonatype OSSRH 仓库配置

6. **必需插件**
   - `maven-source-plugin`: 生成源码 JAR
   - `maven-javadoc-plugin`: 生成文档 JAR
   - `maven-gpg-plugin`: GPG 签名
   - `nexus-staging-maven-plugin`: Nexus 部署

### 故障排除

#### GPG 签名问题
```bash
# 如果 GPG 签名失败，检查密钥
gpg --list-secret-keys

# 设置默认密钥
export GPG_TTY=$(tty)
```

#### 权限问题
```bash
# 确保有正确的 Sonatype 权限
# 检查 JIRA 票据状态
```

#### 网络问题
```bash
# 如果上传失败，可以重试
mvn nexus-staging:deploy-staged
```

### 发布后验证

1. **检查 Maven 中央仓库**
   - 访问 [Maven Central](https://search.maven.org/)
   - 搜索 `io.github.nemoob.atlas-chain`

2. **测试依赖**
   ```xml
   <dependency>
       <groupId>io.github.nemoob</groupId>
       <artifactId>atlas-chain-core</artifactId>
       <version>0.0.1</version>
   </dependency>
   ```

### 版本管理

#### 语义化版本
- `MAJOR.MINOR.PATCH` (例如: 1.0.0)
- `MAJOR`: 不兼容的 API 变更
- `MINOR`: 向后兼容的功能新增
- `PATCH`: 向后兼容的问题修复

#### 快照版本
- 开发版本使用 `-SNAPSHOT` 后缀
- 例如: `0.0.2-SNAPSHOT`

### 自动化发布

可以使用 GitHub Actions 自动化发布流程：

```yaml
# .github/workflows/release.yml
name: Release to Maven Central

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 8
      uses: actions/setup-java@v2
      with:
        java-version: '8'
        distribution: 'adopt'
    - name: Release to Maven Central
      run: mvn clean deploy -P release
      env:
        MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
        GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
        GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

### 注意事项

1. **首次发布**需要等待 Sonatype 审核（通常 1-2 个工作日）
2. **GPG 签名**是必需的，确保密钥已正确配置
3. **所有 artifacts** 必须包含源码和文档 JAR
4. **POM 信息**必须完整，包括许可证、开发者、SCM 等
5. **版本号**一旦发布不能修改，只能发布新版本

### 联系支持

如果遇到问题，可以：
1. 查看 [Sonatype 官方文档](https://central.sonatype.org/publish/)
2. 在 Sonatype JIRA 创建支持票据
3. 参考 [Maven 中央仓库指南](https://maven.apache.org/repository/guide-central-repository-upload.html)