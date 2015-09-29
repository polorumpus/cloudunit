/*
 * LICENCE : CloudUnit is available under the Gnu Public License GPL V3 : https://www.gnu.org/licenses/gpl.txt
 *     but CloudUnit is licensed too under a standard commercial license.
 *     Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 *     If you are not sure whether the GPL is right for you,
 *     you can always test our software under the GPL and inspect the source code before you contact us
 *     about purchasing a commercial license.
 *
 *     LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
 *     or promote products derived from this project without prior written permission from Treeptik.
 *     Products or services derived from this software may not be called "CloudUnit"
 *     nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 *     For any questions, contact us : contact@treeptik.fr
 */

//
//    LICENCE : CloudUnit is available under the Gnu Public License GPL V3 : https://www.gnu.org/licenses/gpl.txt
//    but CloudUnit is licensed too under a standard commercial license.
//    Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
//    If you are not sure whether the GPL is right for you,
//    you can always test our software under the GPL and inspect the source code before you contact us
//    about purchasing a commercial license.
//
//    LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
//    or promote products derived from this project without prior written permission from Treeptik.
//    Products or services derived from this software may not be called "CloudUnit"
//    nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
//    For any questions, contact us : contact@treeptik.fr
//

package fr.treeptik.cloudunit.aspects;

import fr.treeptik.cloudunit.exception.MonitorException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Application;
import fr.treeptik.cloudunit.model.Message;
import fr.treeptik.cloudunit.model.Module;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.MessageService;
import fr.treeptik.cloudunit.utils.MessageUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;

@Aspect
@Component
public class ModuleAspect
                extends CloudUnitAbstractAspect
                implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final String initModule = "INITMODULE";

    private final String createType = "CREATE";

    private final String deleteType = "REMOVE";

    private final Logger logger = LoggerFactory.getLogger( ModuleAspect.class );

    @Inject
    private MessageService messageService;

    @Before( "execution(* fr.treeptik.cloudunit.service.ModuleService.remove(..)) " +
                    "|| execution(* fr.treeptik.cloudunit.service.ModuleService.initModule(..))" )
    public void beforeModule( JoinPoint joinPoint )
                    throws MonitorException, ServiceException
    {

        User user = this.getAuthentificatedUser();
        Message message = null;

        Module module = null;
        switch ( joinPoint.getSignature().getName().toUpperCase() )
        {

            case this.initModule:
                Application application = (Application) joinPoint.getArgs()[0];
                module = (Module) joinPoint.getArgs()[1];
                if ( !module.getName().contains( "git" ) )
                {
                    message = MessageUtils.writeBeforeModuleMessage( user,
                                                                     module.getName(), application.getName(),
                                                                     this.createType );
                    this.logger.info( message.toString() );
                    this.messageService.create( message );
                }
                break;

            case this.deleteType:
                module = (Module) joinPoint.getArgs()[2];
                if ( !module.getName().contains( "git" ) )
                {
                    message = MessageUtils
                                    .writeBeforeModuleMessage( user, module.getName(),
                                                               ( (User) joinPoint.getArgs()[1] ).getLogin(),
                                                               this.deleteType );
                    this.logger.info( message.toString() );
                    this.messageService.create( message );
                }
                break;

        }

    }

    @AfterReturning( pointcut = "execution(* fr.treeptik.cloudunit.service.ModuleService.remove(..)) " +
                    "&& execution(* fr.treeptik.cloudunit.service.ModuleService.initModule(..))",
                    returning = "result" )
    public void afterReturningModule( JoinPoint.StaticPart staticPart, Object result )
                    throws MonitorException
    {
        try
        {
            if ( result == null )
                return;
            Module module = (Module) result;
            User user = module.getApplication().getUser();
            // scape tool module
            if ( !module.getImage().getImageType().equalsIgnoreCase( "tool" ) )
            {
                Message message = null;
                switch ( staticPart.getSignature().getName().toUpperCase() )
                {
                    case this.initModule:
                        message = MessageUtils.writeModuleMessage( user, module,
                                                                   this.createType );
                        break;

                    case this.deleteType:
                        message = MessageUtils.writeModuleMessage( user, module,
                                                                   this.deleteType );
                        break;
                }
                if ( message != null )
                {
                    this.logger.info( message.toString() );
                    this.messageService.create( message );
                }
            }
        }
        catch ( ServiceException e )
        {
            throw new MonitorException( "Error afterReturningApplication", e );

        }
    }

    @AfterThrowing( pointcut = "execution(* fr.treeptik.cloudunit.service.ModuleService.remove(..)) " +
                    "|| execution(* fr.treeptik.cloudunit.service.ModuleService.initModule(..))",
                    throwing = "e" )
    public void afterThrowingModule( JoinPoint.StaticPart staticPart,
                                     Exception e )
                    throws ServiceException
    {
        User user = getAuthentificatedUser();
        Message message = null;
        this.logger.debug( "CALLED CLASS : " + staticPart.getSignature().getName() );
        switch ( staticPart.getSignature().getName().toUpperCase() )
        {
            case this.initModule:
                message = MessageUtils.writeAfterThrowingModuleMessage( e, user,
                                                                        this.initModule );
                break;
            case this.deleteType:
                message = MessageUtils.writeAfterThrowingModuleMessage( e, user,
                                                                        this.deleteType );
                break;
        }
        if ( message != null )
        {
            this.messageService.create( message );
        }
    }

}
