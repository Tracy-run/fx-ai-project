package com.fx.software.core.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import java.io.File;
import java.nio.charset.Charset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.Charset;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

/**
 * @FileName EfkJsonPatternLayout
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Plugin(
        name = "EfkJsonPatternLayout",
        category = "Core",
        elementType = "layout",
        printObject = true
)
public class EfkJsonPatternLayout extends AbstractStringLayout{


    private static String projectPath = (new File("")).getAbsolutePath();
    private PatternLayout patternLayout;
    private String app;

    private EfkJsonPatternLayout(Configuration config, RegexReplacement replace, String eventPattern, PatternSelector patternSelector, Charset charset, boolean alwaysWriteExceptions, boolean noConsoleNoAnsi, String headerPattern, String footerPattern, String app) {
        super(config, charset, PatternLayout.newSerializerBuilder().setConfiguration(config).setReplace(replace).setPattern(headerPattern).setDefaultPattern((String)null).setPatternSelector(patternSelector).setAlwaysWriteExceptions(alwaysWriteExceptions).setNoConsoleNoAnsi(noConsoleNoAnsi).build(), PatternLayout.newSerializerBuilder().setConfiguration(config).setReplace(replace).setPattern(footerPattern).setDefaultPattern((String)null).setPatternSelector(patternSelector).setAlwaysWriteExceptions(alwaysWriteExceptions).setNoConsoleNoAnsi(noConsoleNoAnsi).build());
        this.app = app;
        this.patternLayout = PatternLayout.newBuilder().withPattern(eventPattern).withPatternSelector(patternSelector).withConfiguration(config).withRegexReplacement(replace).withCharset(charset).withAlwaysWriteExceptions(alwaysWriteExceptions).withNoConsoleNoAnsi(noConsoleNoAnsi).withHeader(headerPattern).withFooter(footerPattern).build();
    }

    @PluginFactory
    public static EfkJsonPatternLayout createLayout(@PluginAttribute(value = "pattern",defaultString = "%m%n") String pattern, @PluginElement("PatternSelector") PatternSelector patternSelector, @PluginConfiguration Configuration config, @PluginElement("Replace") RegexReplacement replace, @PluginAttribute("charset") Charset charset, @PluginAttribute(value = "alwaysWriteExceptions",defaultBoolean = true) boolean alwaysWriteExceptions, @PluginAttribute(value = "noConsoleNoAnsi",defaultBoolean = false) boolean noConsoleNoAnsi, @PluginAttribute("header") String headerPattern, @PluginAttribute("footer") String footerPattern, @PluginAttribute("app") String app) {
        return new EfkJsonPatternLayout(config, replace, pattern, patternSelector, charset, alwaysWriteExceptions, noConsoleNoAnsi, headerPattern, footerPattern, app);
    }


    @Override
    public String toSerializable(LogEvent event) {
        String message = this.patternLayout.toSerializable(event);
        com.fx.software.core.log.EfkJsonPatternLayout.LogInfo logInfo = new com.fx.software.core.log.EfkJsonPatternLayout.LogInfo();
        logInfo.setTime(System.currentTimeMillis());
        logInfo.setLevel(event.getLevel().name());
        ReadOnlyStringMap contextMap = event.getContextData();
        if (null != contextMap) {
            logInfo.setApp(this.app);
            logInfo.setRequestId((String)contextMap.getValue("requestId"));
            logInfo.setSpanId((String)contextMap.getValue("spanId"));
            logInfo.setTraceId((String)contextMap.getValue("traceId"));
            logInfo.setUserAccount((String)contextMap.getValue("userAccount"));
        }

        Marker marker = event.getMarker();
        if (null != marker) {
            logInfo.setMarker(marker.getName());
        }

        logInfo.setThread(event.getThreadName());
        logInfo.setLine(event.getSource().toString());
        logInfo.setMessage(message);
        logInfo.setProjectPath(projectPath);

        try {
            String infoJson = (new ObjectMapper()).writeValueAsString(logInfo);
            return infoJson + "\n";
        } catch (JsonProcessingException var7) {
            var7.printStackTrace();
            return "\n";
        }
    }



    class LogInfo {
        long time;
        String level;
        String projectPath;
        String app;
        String thread;
        String line;
        String requestId;
        String traceId;
        String spanId;
        String message;
        String marker;
        String userAccount;

        public LogInfo() {
        }

        public long getTime() {
            return this.time;
        }

        public String getLevel() {
            return this.level;
        }

        public String getProjectPath() {
            return this.projectPath;
        }

        public String getApp() {
            return this.app;
        }

        public String getThread() {
            return this.thread;
        }

        public String getLine() {
            return this.line;
        }

        public String getRequestId() {
            return this.requestId;
        }

        public String getTraceId() {
            return this.traceId;
        }

        public String getSpanId() {
            return this.spanId;
        }

        public String getMessage() {
            return this.message;
        }

        public String getMarker() {
            return this.marker;
        }

        public String getUserAccount() {
            return this.userAccount;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public void setThread(String thread) {
            this.thread = thread;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public void setSpanId(String spanId) {
            this.spanId = spanId;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setMarker(String marker) {
            this.marker = marker;
        }

        public void setUserAccount(String userAccount) {
            this.userAccount = userAccount;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof LogInfo)) {
                return false;
            } else {
                LogInfo other = (LogInfo) o;
                if (!other.canEqual(this)) {
                    return false;
                } else if (this.getTime() != other.getTime()) {
                    return false;
                } else {
                    label145:
                    {
                        Object this$level = this.getLevel();
                        Object other$level = other.getLevel();
                        if (this$level == null) {
                            if (other$level == null) {
                                break label145;
                            }
                        } else if (this$level.equals(other$level)) {
                            break label145;
                        }

                        return false;
                    }

                    Object this$projectPath = this.getProjectPath();
                    Object other$projectPath = other.getProjectPath();
                    if (this$projectPath == null) {
                        if (other$projectPath != null) {
                            return false;
                        }
                    } else if (!this$projectPath.equals(other$projectPath)) {
                        return false;
                    }

                    Object this$app = this.getApp();
                    Object other$app = other.getApp();
                    if (this$app == null) {
                        if (other$app != null) {
                            return false;
                        }
                    } else if (!this$app.equals(other$app)) {
                        return false;
                    }

                    label124:
                    {
                        Object this$thread = this.getThread();
                        Object other$thread = other.getThread();
                        if (this$thread == null) {
                            if (other$thread == null) {
                                break label124;
                            }
                        } else if (this$thread.equals(other$thread)) {
                            break label124;
                        }

                        return false;
                    }

                    Object this$line = this.getLine();
                    Object other$line = other.getLine();
                    if (this$line == null) {
                        if (other$line != null) {
                            return false;
                        }
                    } else if (!this$line.equals(other$line)) {
                        return false;
                    }

                    Object this$requestId = this.getRequestId();
                    Object other$requestId = other.getRequestId();
                    if (this$requestId == null) {
                        if (other$requestId != null) {
                            return false;
                        }
                    } else if (!this$requestId.equals(other$requestId)) {
                        return false;
                    }

                    label103:
                    {
                        Object this$traceId = this.getTraceId();
                        Object other$traceId = other.getTraceId();
                        if (this$traceId == null) {
                            if (other$traceId == null) {
                                break label103;
                            }
                        } else if (this$traceId.equals(other$traceId)) {
                            break label103;
                        }

                        return false;
                    }

                    Object this$spanId = this.getSpanId();
                    Object other$spanId = other.getSpanId();
                    if (this$spanId == null) {
                        if (other$spanId != null) {
                            return false;
                        }
                    } else if (!this$spanId.equals(other$spanId)) {
                        return false;
                    }

                    label89:
                    {
                        Object this$message = this.getMessage();
                        Object other$message = other.getMessage();
                        if (this$message == null) {
                            if (other$message == null) {
                                break label89;
                            }
                        } else if (this$message.equals(other$message)) {
                            break label89;
                        }

                        return false;
                    }

                    Object this$marker = this.getMarker();
                    Object other$marker = other.getMarker();
                    if (this$marker == null) {
                        if (other$marker != null) {
                            return false;
                        }
                    } else if (!this$marker.equals(other$marker)) {
                        return false;
                    }

                    Object this$userAccount = this.getUserAccount();
                    Object other$userAccount = other.getUserAccount();
                    if (this$userAccount == null) {
                        if (other$userAccount == null) {
                            return true;
                        }
                    } else if (this$userAccount.equals(other$userAccount)) {
                        return true;
                    }

                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof LogInfo;
        }

        @Override
        public int hashCode() {
            int result = 1;
            long $time = this.getTime();
            result = result * 59 + (int) ($time >>> 32 ^ $time);
            Object $level = this.getLevel();
            result = result * 59 + ($level == null ? 43 : $level.hashCode());
            Object $projectPath = this.getProjectPath();
            result = result * 59 + ($projectPath == null ? 43 : $projectPath.hashCode());
            Object $app = this.getApp();
            result = result * 59 + ($app == null ? 43 : $app.hashCode());
            Object $thread = this.getThread();
            result = result * 59 + ($thread == null ? 43 : $thread.hashCode());
            Object $line = this.getLine();
            result = result * 59 + ($line == null ? 43 : $line.hashCode());
            Object $requestId = this.getRequestId();
            result = result * 59 + ($requestId == null ? 43 : $requestId.hashCode());
            Object $traceId = this.getTraceId();
            result = result * 59 + ($traceId == null ? 43 : $traceId.hashCode());
            Object $spanId = this.getSpanId();
            result = result * 59 + ($spanId == null ? 43 : $spanId.hashCode());
            Object $message = this.getMessage();
            result = result * 59 + ($message == null ? 43 : $message.hashCode());
            Object $marker = this.getMarker();
            result = result * 59 + ($marker == null ? 43 : $marker.hashCode());
            Object $userAccount = this.getUserAccount();
            result = result * 59 + ($userAccount == null ? 43 : $userAccount.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "EfkJsonPatternLayout.LogInfo(time=" + this.getTime() + ", level=" + this.getLevel() + ", projectPath=" + this.getProjectPath() + ", app=" + this.getApp() + ", thread=" + this.getThread() + ", line=" + this.getLine() + ", requestId=" + this.getRequestId() + ", traceId=" + this.getTraceId() + ", spanId=" + this.getSpanId() + ", message=" + this.getMessage() + ", marker=" + this.getMarker() + ", userAccount=" + this.getUserAccount() + ")";
        }
    }
}
