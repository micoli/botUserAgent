package org.micoli.commandRunner.soundTTS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.config.MaryConfig;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.tools.install.ComponentDescription;
import marytts.tools.install.InstallFileParser;
import marytts.tools.install.LanguageComponentDescription;
import marytts.tools.install.VoiceComponentDescription;
import marytts.util.MaryUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.micoli.api.ClassPathHacker;
import org.micoli.api.ServiceProviderTools;
import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.CommandRoute;
import org.micoli.botUserAgent.AudioPlugin;
import org.micoli.botUserAgent.BotExtension;
import org.micoli.commandRunner.soundTTS.http.Downloader;
import org.micoli.commandRunner.soundTTS.http.RBCWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class SoundTTSPlugin extends Plugin {
	protected final static Logger logger = LoggerFactory.getLogger(SoundTTSPlugin.class);
	private static MaryInterface marytts;
	private static Map<String,ComponentDescription> components = new HashMap<String,ComponentDescription>();
	protected static String pluginPath;
	protected static PluginWrapper pluginWrapper;

	public SoundTTSPlugin(PluginWrapper wrapper) {
		super(wrapper);
		pluginPath = getWrapper().getPluginPath();
		pluginWrapper = getWrapper();
	}

	private static void init(){
		System.setProperty("mary.installedDir"	,System.getProperty("pf4j.pluginsDir")+pluginPath );
		System.setProperty("mary.downloadDir"	,"/tmp/") ;
		System.setProperty("mary.base"			,System.getProperty("mary.installedDir"));
		logger.info("Init : pluginPath :"+System.getProperty("pf4j.pluginsDir")+pluginPath);
	}

	private static InstallFileParser getInstallationConfig(){
		URL url;
		try {
			init();
			url = new URL("https://raw.github.com/marytts/marytts/master/download/marytts-components.xml");
			return new InstallFileParser(url);
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject getDownloadableList(){
		int idx;

		InstallFileParser installFileParser =  getInstallationConfig();
		JSONArray voicesList = new JSONArray();
		idx=0;
		for(VoiceComponentDescription voiceDescription : installFileParser.getVoiceDescriptions()){
			JSONObject jsonVoice = new JSONObject();
			List<URL> locations = voiceDescription.getLocations();
			String code=String.format("v%02d",idx++);
			jsonVoice.put("name"			, voiceDescription.getName());
			jsonVoice.put("gender"			, voiceDescription.getGender());
			jsonVoice.put("description"		, voiceDescription.getDescription());
			jsonVoice.put("language"		, voiceDescription.getLocale().getLanguage());
			jsonVoice.put("packageSize"		, voiceDescription.getDisplayPackageSize());
			jsonVoice.put("packageFilename"	, voiceDescription.getPackageFilename());
			jsonVoice.put("version"			, voiceDescription.getVersion());
			jsonVoice.put("url"				, locations);
			if(locations.size()>0){
				jsonVoice.put("code"		, code);
				components.put(code, voiceDescription);
			}
			voicesList.add(jsonVoice);
			idx++;
		}
		JSONArray languagesList = new JSONArray();
		idx=0;
		for(LanguageComponentDescription languageDescription : installFileParser.getLanguageDescriptions()){
			JSONObject jsonVoice = new JSONObject();
			List<URL> locations = languageDescription.getLocations();
			String code=String.format("v%02d",idx++);
			jsonVoice.put("name"			, languageDescription.getName());
			jsonVoice.put("description"		, languageDescription.getDescription());
			jsonVoice.put("language"		, languageDescription.getLocale().getLanguage());
			jsonVoice.put("packageSize"		, languageDescription.getDisplayPackageSize());
			jsonVoice.put("packageFilename"	, languageDescription.getPackageFilename());
			jsonVoice.put("version"			, languageDescription.getVersion());
			jsonVoice.put("url"				, languageDescription.getLocations());
			if(locations.size()>0){
				jsonVoice.put("code"		, code);
				components.put(code, languageDescription);
			}
			languagesList.add(jsonVoice);
		}
		JSONObject jsonResult = new JSONObject();
		jsonResult.put("voices"		, voicesList);
		jsonResult.put("languages"	, languagesList);
		return jsonResult;
	}

	@Override
	public void start() {
		logger.debug("SoundTTSPlugin.start()");
		init();
		loadConfigs(System.getProperty("pf4j.pluginsDir")+pluginPath+"/lib",false);
		ClassPathHacker.setClassLoader(this.getWrapper().getPluginClassLoader());

		//System.setProperty("mary.base"			,"/tmp/voices");
		//loadVoices();
		//loadConfigs("/tmp/voices/lib",false);
		//ClassPathHacker.resetClassLoader();

		displayConfigs();
		logger.debug("End initialisation");
	}

	public static void loadVoices() {
		File dir = new File("/tmp/voices/lib");
		logger.debug("Scanning /tmp/voices, looking for voice providers");
		if(dir.exists() && dir.isDirectory()){
			dir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name){
					if(name.endsWith(".jar")){
						try {
							logger.debug("add file /tmp/voices/lib/"+name);
							ClassPathHacker.addFile("/tmp/voices/lib/"+name);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return false;
				}
			});
		}
	}

	public static void displayConfigs() {
		try{
			marytts = new LocalMaryInterface();
			for(Locale locale : marytts.getAvailableLocales()){
				logger.info("### Locale "+locale.getCountry()+"-"+locale.getDisplayLanguage()+"::"+locale.getDisplayName());
			}
			for(String voice : marytts.getAvailableVoices()){
				logger.info("### Voice "+voice);
			}
		} catch (MaryConfigurationException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
	}

	public static void loadConfigs(String path,boolean loadJar) {
		logger.debug("Init MaryConfig classes :"+path+", "+loadJar );
		final Set<String> MaryConfigClasses = ServiceProviderTools.getProvidersFromJar(path,"marytts.config.MaryConfig",loadJar);

		logger.debug("Init MaryConfig classes :"+MaryConfigClasses.size() );
		for(String configClass : MaryConfigClasses){
			try {
				logger.info("Load MaryConfig class: "+configClass);
				boolean found=false;
				for(MaryConfig maryConfig : MaryConfig.getConfigs()){
					if(maryConfig.getClass().getName().equals(configClass)){
						found=true;
					}
				}
				if(!found){
					MaryConfig.addConfig((MaryConfig) Class.forName(configClass).getConstructor().newInstance());
				}
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
		}
	}

	@Override
	public void stop() {
		logger.debug("SoundTTSPlugin.stop()");
	}

	@Extension
	public static class SoundTTS implements BotExtension{

		@CommandRoute(value="sayWords", args={"words","callId"})
		public void sayWord(final CommandArgs args){
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(args.get("callId").getBytes(),0,args.get("callId").length());
				final String tmpFileName="/tmp/"+String.format("%032x", new BigInteger(1, md5.digest()))+".raw";

				new java.util.Timer().schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						try {
							//logger.debug("Voice "+marytts.getLocale().getLanguage()+"::"+marytts.getLocale().toLanguageTag());
							marytts.setLocale(new Locale(MaryUtils.string2locale("fr").getLanguage()));
							logger.info(String.format("Generating %s for callId: %s, saying '%s' in '%s'",tmpFileName,args.get("callId"),args.get("words"),marytts.getLocale().toLanguageTag()));

							//PCM 8kHz, 16 bits signed, mono-channel, little endian
							saveToFile(getAudioInputStream(new AudioFormat(8000, 16,1,true,false), marytts.generateAudio(args.get("words"))),tmpFileName);
							AudioSystem.write(
									marytts.generateAudio(args.get("words"))
								,AudioFileFormat.Type.WAVE
								,new File(tmpFileName+".wav"));
							args.getContext(AudioPlugin.class).playAudioFile(args.get("callId"), tmpFileName);
						} catch (SynthesisException e) {
							logger.error(e.getClass().getSimpleName(), e);
						} catch (IOException e) {
							logger.error(e.getClass().getSimpleName(), e);
						}
					}
				},200);
			} catch (SecurityException| IllegalArgumentException | NoSuchAlgorithmException e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
		}

		@CommandRoute(value="setVoice", args={"voice"})
		public void setVoice(final CommandArgs args){
			setVoice(args.get("voice"));
		}

		public void setVoice(String name){
			logger.debug("setVoice "+name);
			Set<String> voices = marytts.getAvailableVoices();
			Iterator<String> iterator = voices.iterator();
			while (iterator.hasNext()){
				String voiceName = iterator.next();
				if(voiceName.equals(name)){
					logger.debug("setVoice found "+name);
					marytts.setVoice(voiceName);
					return;
				}
			}
		}

		private AudioInputStream getAudioInputStream(AudioFormat targ,AudioInputStream ais){
			ClassLoader loader = ClassLoader.getSystemClassLoader();

			logger.debug("Looking for FormatConversionProvider supproting : "+targ.toString());
			final Set<String> ProviderClasses=ServiceProviderTools.getProvidersFromClassLoader(loader, "javax.sound.sampled.spi.FormatConversionProvider");
			for(String classe: ProviderClasses) {
				FormatConversionProvider provider;
				try {
					logger.debug("New instance of provider : "+classe+"::");
					provider = (FormatConversionProvider) Class.forName(classe).getConstructor().newInstance();
					if (!provider.isConversionSupported(targ,ais.getFormat())){
						continue;
					}
					logger.info("Found FormatConversionProvider : "+classe);
					return provider.getAudioInputStream(targ,ais);
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException
						| ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			throw new IllegalArgumentException("Encoding not supported for stream");
		}

		private void saveToFile(AudioInputStream inStream,String outFileName){
			File outputFile=new File(outFileName);

			if (!outputFile.exists()) {
				try {
					outputFile.createNewFile();
				}catch (IOException e) {
					logger.error(e.getClass().getSimpleName(), e);
				}
			}
			byte[] buf = new byte[1024];
			try{
				int bytes_read = 0;
				FileOutputStream out = new FileOutputStream(outputFile);

				do {
					bytes_read = inStream.read(buf, 0, buf.length);
					if (bytes_read > 0){
						out.write(buf, 0, bytes_read);
					}
				} while (bytes_read >= 0);

				try {
					out.close();
				}catch (Exception e) {
					logger.error(e.getClass().getSimpleName(), e);
				}
				out.close();

			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	/*}
	 *
	@Extension
	public static class MaryTTS implements GlobalExtension{*/
		private static boolean isDownloading = false;
		@CommandRoute(value="getdwns", args={},global=true)
		public String getDwns(CommandArgs args){
			return getDownloadableList().toString();
		}

		@CommandRoute(value="getdwnstxt", args={},global=true)
		// bot from=6000 action=getdwnstxt
		// bot from=6000 action=dwn code=v40
		public String getDwnstxt(CommandArgs args){
			String result="";
			JSONObject main = getDownloadableList();
			result=result+"Voices:\n";
			for(Object object: ((JSONArray) main.get("voices")).toArray()){
				JSONObject voice = (JSONObject) object;
				result=result+String.format("  - %s [%s,%s] %s",voice.get("code"),voice.get("packageFilename"),voice.get("packageSize"),voice.get("description"))+"\n";
			}
			result=result+"Languages:\n";
			for(Object object: ((JSONArray) main.get("languages")).toArray()){
				JSONObject language = (JSONObject) object;
				result=result+String.format("  - %s [%s,%s] %s",language.get("code"),language.get("packageFilename"),language.get("packageSize"),language.get("description"))+"\n";
			}

			return result;
		}

		@CommandRoute(value="dwn", args={"code"},global=true)
		public String dwn(CommandArgs args){
			getDownloadableList();

			if(components.containsKey(args.get("code"))){
				ComponentDescription componentDescription = components.get(args.get("code"));

				String url = componentDescription.getLocations().get(0).toString();
				String[] tmps = url.split("/");
				String tmpFile = System.getProperty("mary.downloadDir")+tmps[tmps.length-1];;
				if(isDownloading){
					return "Already in downloading";
				}else{
					File oFile = new File(tmpFile);
					if(oFile.exists() && oFile.length()==componentDescription.getPackageSize()){
						logger.info("Already downloaded");
					}else{
						try{
							isDownloading=true;
							new Downloader(){
								public void rbcProgressCallback(RBCWrapper rbc, double progress) {
									logger.info( String.format( "Download progress for %s : %d bytes received, %.02f%%",rbc.getUrl(), rbc.getReadSoFar(), progress));
								}
							}.start(tmpFile,url);
						}finally{
							isDownloading=false;
						}
					}
					try {
						componentDescription.install(true);
						loadConfigs(System.getProperty("pf4j.pluginsDir")+pluginPath+"/lib",false);
						displayConfigs();
					} catch (Exception e) {
						e.printStackTrace();
					}

					return "OK";
				}
			}else{
				return "Not OK, Unknown code";
			}
		}

		@CommandRoute(value="print", args={"text"})
		public String print(CommandArgs args){
			String result = "PRINT: "+args.get("text")+", context: "+args.getContext();
			logger.info(result);
			return result;
		}
	}
}